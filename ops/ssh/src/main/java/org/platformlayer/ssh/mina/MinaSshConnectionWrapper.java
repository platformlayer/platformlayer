package org.platformlayer.ssh.mina;

import java.io.Closeable;
import java.io.IOException;
import java.security.KeyPair;

import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.apache.sshd.client.ServerKeyVerifier;
import org.apache.sshd.client.channel.ChannelSession;
import org.apache.sshd.client.future.ConnectFuture;
import org.platformlayer.ExceptionUtils;
import org.platformlayer.ops.ssh.IServerKeyVerifier;
import org.platformlayer.ops.ssh.SshException;
import org.platformlayer.ssh.SshConnectionInfo;
import org.platformlayer.ssh.mina.bugfix.BugFixChannelExec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.TimeSpan;

public class MinaSshConnectionWrapper implements Closeable {
	private static final Logger log = LoggerFactory.getLogger(MinaSshConnectionWrapper.class);

	final SshConnectionInfo connectionInfo;

	ClientSession sshClientSession;
	SshConnectionState state;

	private final MinaSshContext sshContext;

	private IServerKeyVerifier serverKeyVerifier;

	enum SshConnectionState {
		NotConnected, Connected, Authenticated, Closed
	}

	MinaSshConnectionWrapper(MinaSshContext sshContext, SshConnectionInfo connectionInfo) {
		super();
		this.sshContext = sshContext;
		this.connectionInfo = connectionInfo;
		this.state = SshConnectionState.NotConnected;
	}

	public SshConnectionInfo getConnectionInfo() {
		return connectionInfo;
	}

	@Override
	public void close() {
		if (sshClientSession != null) {
			sshClientSession.close(true);
			sshClientSession = null;
		}
		this.state = SshConnectionState.Closed;
	}

	public boolean isConnected() {
		switch (state) {
		case Authenticated:
		case Connected:
			return true;
		case NotConnected:
			return false;
		case Closed:
			return false;
		default:
			throw new IllegalStateException();
		}
	}

	public boolean isAuthenticationComplete() {
		switch (state) {
		case Authenticated:
			return true;
		case Connected:
			return false;
		case NotConnected:
		case Closed:
			throw new IllegalStateException();
		default:
			throw new IllegalStateException();
		}
	}

	public ChannelSession openSession(String command) throws SshException {
		try {
			boolean useAgentForwarding = sshContext.isAgentForwarding();

			ChannelSession clientChannel = BugFixChannelExec.createExecChannel(sshClientSession, command,
					useAgentForwarding);
			return clientChannel;
		} catch (Exception e) {
			ExceptionUtils.handleInterrupted(e);
			throw new SshException("Error creating channel", e);
		}
	}

	public boolean isConnectionClosed() {
		switch (state) {
		case Authenticated:
		case Connected:
			return false;
		case Closed:
			return true;
		default:
			throw new IllegalStateException();
		}
	}

	public static MinaSshConnectionWrapper build(MinaSshContext sshContext, SshConnectionInfo connectionInfo)
			throws SshException {
		return new MinaSshConnectionWrapper(sshContext, connectionInfo);
	}

	public void connect(TimeSpan connectTimeout) throws SshException {
		if (state != SshConnectionState.NotConnected) {
			throw new IllegalStateException();
		}

		try {
			SshClient client = sshContext.client;

			System.out.println("New SSH connection to " + connectionInfo.getHost());

			ConnectFuture connect = client.connect(connectionInfo.getSocketAddress());
			if (!connect.await(connectTimeout.getTotalMilliseconds())) {
				connect.cancel();
				throw new SshException("Timeout while waiting for SSH connection to " + connectionInfo.getHost());
			}

			this.sshClientSession = connect.getSession();

			if (this.sshClientSession == null) {
				throw new IllegalStateException();
			}

			this.state = SshConnectionState.Connected;
		} catch (Exception e) {
			ExceptionUtils.handleInterrupted(e);
			throw new SshException("Error connecting to SSH server @" + connectionInfo.getSocketAddress(), e);
		}
	}

	public boolean authenticateWithPublicKey(String user, KeyPair key, TimeSpan authenticationTimeout)
			throws SshException {
		if (state != SshConnectionState.Connected) {
			throw new IllegalStateException();
		}

		// DelegatingServerKeyVerifier picks this up
		if (serverKeyVerifier != null) {
			sshClientSession.getMetadataMap().put(ServerKeyVerifier.class,
					new ServerKeyVerifierAdapter(serverKeyVerifier));
		}

		// The 0xfff mask with -1 timeout trick lets us get the current state
		int sshSessionStateCode = sshClientSession.waitFor(0xfff, -1);

		if ((sshSessionStateCode & ClientSession.AUTHED) == 0) {
			try {
				sshClientSession.authPublicKey(user, key);
			} catch (IOException e) {
				throw new SshException("I/O error while authenticating " + this, e);
			} catch (IllegalStateException e) {
				throw new SshException("Error authenticating " + this, e);
			}

			sshSessionStateCode = sshClientSession.waitFor(ClientSession.CLOSED | ClientSession.AUTHED
					| ClientSession.WAIT_AUTH, authenticationTimeout.getTotalMilliseconds());
		}

		if ((sshSessionStateCode & ClientSession.WAIT_AUTH) != 0) {
			throw new SshException("SSH authentication failed (trying " + connectionInfo + ")");
		}

		if ((sshSessionStateCode & ClientSession.CLOSED) != 0) {
			this.sshClientSession = null;
			state = SshConnectionState.Closed;
			throw new SshException("SSH connection closed while trying to login");
		}

		// if (serverKeyVerifier != null) {
		// byte[] serverKey = getServerKey(sshClientSession);
		// SocketAddress remoteAddress = new InetSocketAddress(connectionInfo.getHost(), connectionInfo.getPort());
		// if (!serverKeyVerifier.verifyServerKey(remoteAddress, serverKey)) {
		// sshClientSession.close(true);
		// throw new SshException("SSH key verification failed");
		// }
		// }

		if ((sshSessionStateCode & ClientSession.AUTHED) != 0) {
			state = SshConnectionState.Authenticated;
			return true;
		}

		if (sshSessionStateCode == ClientSession.TIMEOUT) {
			throw new SshException("Timeout while waiting for session");
		}
		throw new SshException("Unexpected state; not authenticated; not closed");
	}

	@Override
	public String toString() {
		return "Ssh connection: " + connectionInfo + "; Current state: " + state.toString();
	}

	public IServerKeyVerifier getServerKeyVerifier() {
		return serverKeyVerifier;
	}

	public void setServerKeyVerifier(IServerKeyVerifier serverKeyVerifier) {
		this.serverKeyVerifier = serverKeyVerifier;
	}
}
