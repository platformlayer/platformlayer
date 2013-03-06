package org.platformlayer.ssh.mina;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyPair;

import org.apache.sshd.ClientChannel;
import org.apache.sshd.agent.SshAgent;
import org.apache.sshd.agent.local.LocalAgentFactory;
import org.apache.sshd.client.channel.ChannelSession;
import org.apache.sshd.client.channel.ForwardLocalPort;
import org.apache.sshd.client.channel.SshTunnelSocket;
import org.apache.sshd.common.RuntimeSshException;
import org.platformlayer.ExceptionUtils;
import org.platformlayer.ops.process.ProcessExecution;
import org.platformlayer.ops.ssh.IServerKeyVerifier;
import org.platformlayer.ops.ssh.SshConnection;
import org.platformlayer.ops.ssh.SshException;
import org.platformlayer.ops.ssh.SshPortForward;
import org.platformlayer.ssh.SshConnectionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.TimeSpan;

public class MinaSshConnection extends SshConnection {
	private static final Logger log = LoggerFactory.getLogger(MinaSshConnection.class);

	public static final TimeSpan DEFAULT_SSH_EXECUTE_TIMEOUT = new TimeSpan("15s");

	public static final TimeSpan DEFAULT_SSH_CONNECT_TIMEOUT = new TimeSpan("15s");
	public static final TimeSpan DEFAULT_SSH_KEY_EXECUTE_TIMEOUT = new TimeSpan("90s");

	final MinaSshContext minaSshContext;

	MinaSshConnectionWrapper sshConnection;

	KeyPair keyPair;

	MinaSshContext getSshContext() {
		return minaSshContext;
	}

	@Override
	public synchronized void close() {
		if (sshConnection != null) {
			sshConnection.close();
		}

		sshConnection = null;
	}

	public synchronized void closeAndRemoveFromPool() {
		close();
	}

	protected MinaSshConnectionWrapper ensureConnected() throws IOException, SshException {
		if (sshConnection == null) {
			getSshConnection();
		}

		activateConnection(sshConnection);

		return sshConnection;
	}

	protected synchronized MinaSshConnectionWrapper getSshConnection() throws IOException, SshException {
		if (getHost() == null) {
			throw new NullPointerException("getHost() returns null");
		}
		if (getKeyPair() == null) {
			throw new NullPointerException("privateKey is required");
		}
		SshConnectionInfo connectionInfo = new SshConnectionInfo(getHost(), getPort(), getUser(), getKeyPair());

		if (sshConnection != null) {
			throw new IllegalStateException("Attempt to get SSH connection when connection not previously closed");
		}

		sshConnection = MinaSshConnectionWrapper.build(getSshContext(), connectionInfo);

		activateConnection(sshConnection);

		return sshConnection;
	}

	private void activateConnection(MinaSshConnectionWrapper connection) throws IOException, SshException {
		activateConnection(connection, DEFAULT_SSH_CONNECT_TIMEOUT, DEFAULT_SSH_KEY_EXECUTE_TIMEOUT);
	}

	private void activateConnection(MinaSshConnectionWrapper sshConnection, TimeSpan connectTimeout,
			TimeSpan keyExchangeTimeout) throws IOException, SshException {
		boolean okay = false;
		try {
			if (!sshConnection.isConnected()) {
				log.info("Making new SSH connection to " + getHost());

				sshConnection.setServerKeyVerifier(this.getServerKeyVerifier());

				if (connectTimeout == null) {
					connectTimeout = TimeSpan.ZERO;
				}
				if (keyExchangeTimeout == null) {
					keyExchangeTimeout = TimeSpan.ZERO;
				}

				sshConnection.connect(connectTimeout);
				if (!sshConnection.isConnected()) {
					throw new IllegalStateException("Connection completed, but could not get connection details");
				}
			} else {
				IServerKeyVerifier myServerKeyVerifier = getServerKeyVerifier();
				myServerKeyVerifier.verifyPooled(sshConnection.getServerKeyVerifier());
			}

			if (!sshConnection.isAuthenticationComplete()) {
				// Authenticate
				boolean isAuthenticated = sshConnection.authenticateWithPublicKey(getUser(), getKeyPair(),
						keyExchangeTimeout);

				if (isAuthenticated == false) {
					// This happens when we upload the wrong public_key...
					// double check if you hit this!
					// This also happens if we're running a command that isn't
					// valid
					throw new SshException("Authentication failed.  Tried to connect to " + getUser() + "@"
							+ sshConnection.getConnectionInfo().getHost());
				} else {
					log.debug("SSH authentication succeeded");
				}
			}

			okay = true;
		} finally {
			if (!okay) {
				// If we fail to activate for any reason, we reset the
				// connection so that we start clean
				log.info("Resetting connection after failure to connect");
				close();
			}
		}
	}

	public MinaSshConnection(MinaSshContext context) {
		super();
		this.minaSshContext = context;
	}

	@Override
	protected ProcessExecution sshExecute0(String command, TimeSpan timeout) throws SshException, IOException,
			InterruptedException {
		try {
			ByteArrayOutputStream stdoutBinary = new ByteArrayOutputStream();
			ByteArrayOutputStream stderrBinary = new ByteArrayOutputStream();

			int exitCode = sshExecute(command, stdoutBinary, stderrBinary, null, timeout);

			ProcessExecution processExecution = new ProcessExecution(command, exitCode, stdoutBinary.toByteArray(),
					stderrBinary.toByteArray());
			return processExecution;
		} catch (RuntimeSshException e) {
			throw new SshException("Unexpected SSH error", e);
		}
	}

	int sshExecute(String command, final OutputStream stdout, final OutputStream stderr, ProcessStartListener listener,
			TimeSpan timeout) throws SshException, IOException, InterruptedException {
		ChannelSession sshChannel = null;
		try {
			sshChannel = ensureConnected().openSession(command);

			sshChannel.setIn(new ByteArrayInputStream(new byte[0]));
			sshChannel.setOut(stdout);
			sshChannel.setErr(stderr);
			try {
				sshChannel.open().await(DEFAULT_SSH_CONNECT_TIMEOUT.getTotalMilliseconds());
			} catch (Exception e) {
				ExceptionUtils.handleInterrupted(e);
				throw new SshException("Ssh error opening channel", e);
			}

			if (listener != null) {
				throw new UnsupportedOperationException();
				// listener.startedProcess(sshChannel.getStdin());
			}

			if (timeout == null) {
				timeout = TimeSpan.ZERO;
			}

			// Wait for everything to finish
			int flags = sshChannel.waitFor(ClientChannel.EOF | ClientChannel.CLOSED, timeout.getTotalMilliseconds());
			if ((flags & ClientChannel.TIMEOUT) != 0) {
				closeAndRemoveFromPool();
				throw new SshException("Timeout while waiting for SSH task to complete.  Timeout was " + timeout);
			}

			flags = sshChannel.waitFor(ClientChannel.EXIT_STATUS, 30000);
			if ((flags & ClientChannel.TIMEOUT) != 0) {
				closeAndRemoveFromPool();
				throw new SshException("Timeout while waiting for exit code.  Timeout was " + timeout);
			}

			Integer exitCode = getExitStatus(sshChannel);

			if (exitCode == null) {
				closeAndRemoveFromPool();
				throw new SshException("No exit code returned");
			}

			return exitCode;
		} finally {
			if (sshChannel != null) {
				sshChannel.close(false);
			}
		}
	}

	private Integer getExitStatus(ClientChannel sshChannel) {
		Integer exitCode = sshChannel.getExitStatus();
		return exitCode;
	}

	@Override
	protected void sshCopyData0(InputStream fileData, long dataLength, String remoteFile, String mode, boolean sudo)
			throws IOException, InterruptedException, SshException {
		int lastSlash = remoteFile.lastIndexOf('/');
		if (lastSlash == -1) {
			throw new IllegalArgumentException("Expected dest file to be absolute path: " + remoteFile);
		}

		MinaSshConnectionWrapper sshConnection = ensureConnected();

		MinaScpClient scp = new MinaScpClient(sshConnection);

		String remoteDir = remoteFile.substring(0, lastSlash);
		String filename = remoteFile.substring(lastSlash + 1);

		try {
			TimeSpan timeout = TimeSpan.FIVE_MINUTES;
			scp.put(fileData, dataLength, filename, remoteDir, mode, timeout, sudo);
		} catch (IOException ioException) {
			throw new SshException("Cannot doing scp on file", ioException);
		} catch (RuntimeSshException e) {
			throw new SshException("Error doing scp on file", e);
		}

	}

	@Override
	protected byte[] sshReadFile0(String remoteFile, boolean sudo) throws IOException, InterruptedException,
			SshException {
		MinaSshConnectionWrapper sshConnection = ensureConnected();

		MinaScpClient scp = new MinaScpClient(sshConnection);
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			TimeSpan timeout = TimeSpan.FIVE_MINUTES;
			scp.get(remoteFile, baos, timeout, sudo);
			return baos.toByteArray();
		} catch (IOException ioException) {
			throw new SshException("Cannot read file", ioException);
		} catch (SshException sshException) {
			String message = sshException.getMessage();
			if (message != null && message.endsWith(": No such file or directory")) {
				return null;
			}
			throw sshException;
		} catch (RuntimeSshException e) {
			throw new SshException("Error reading file", e);
		}
	}

	@Override
	public Socket buildTunneledSocket() throws IOException, SshException {
		MinaSshConnectionWrapper sshConnection = ensureConnected();

		return new SshTunnelSocket(sshConnection.sshClientSession);
	}

	@Override
	public SshConnection buildAgentConnection(KeyPair agentKeyPair) throws IOException, SshException {
		LocalAgentFactory agentFactory = new LocalAgentFactory();
		SshAgent agent = agentFactory.getAgent();
		try {
			agent.addIdentity(agentKeyPair, "default");
		} catch (IOException e) {
			throw new IllegalArgumentException("Error adding agent identity", e);
		}
		MinaSshConnection agentConnection = new MinaSshConnection(new MinaSshContext(agentFactory));

		agentConnection.setHost(this.getHost());
		agentConnection.setPort(this.getPort());
		agentConnection.setUser(this.getUser());
		agentConnection.setServerKeyVerifier(this.getServerKeyVerifier());
		agentConnection.setKeyPair(this.getKeyPair());

		agentConnection.ensureConnected();

		return agentConnection;
	}

	@Override
	public SshPortForward forwardLocalPort(InetSocketAddress remoteSocketAddress) throws IOException, SshException {
		MinaSshConnectionWrapper sshConnection = ensureConnected();

		final ForwardLocalPort forwardLocalPort = new ForwardLocalPort(sshConnection.sshClientSession,
				remoteSocketAddress);

		forwardLocalPort.start();

		return new SshPortForward() {
			@Override
			public void close() throws IOException {
				forwardLocalPort.close();
			}

			@Override
			public InetSocketAddress getLocalSocketAddress() {
				return forwardLocalPort.getLocalSocketAddress();
			}
		};
	}

}
