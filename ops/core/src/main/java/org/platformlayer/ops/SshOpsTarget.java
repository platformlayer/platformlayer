package org.platformlayer.ops;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.fathomdb.Utf8;
import org.platformlayer.ExceptionUtils;
import org.platformlayer.TimeSpan;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.process.ProcessExecution;
import org.platformlayer.ops.process.ProcessExecutionException;
import org.platformlayer.ops.ssh.SshConnection;
import org.platformlayer.ops.ssh.SshException;
import org.platformlayer.ops.ssh.SshPortForward;

import com.google.common.base.Objects;
import com.google.common.net.InetAddresses;

public class SshOpsTarget extends OpsTargetBase {
	private final SshConnection sshConnection;
	private final File tempDirBase;
	private final MachineBase machine;
	boolean ensureRunningAsRoot;

	public SshOpsTarget(File tempDirBase, MachineBase machine, SshConnection sshConnection) {
		this.tempDirBase = tempDirBase;
		this.machine = machine;
		this.sshConnection = sshConnection;
	}

	public InetAddress getHost() {
		return sshConnection.getHost();
	}

	@Override
	public File createTempDir() throws OpsException {
		// TODO: Auto delete tempdir?
		return createTempDir(tempDirBase);
	}

	@Override
	public void doUpload(FileUpload upload) throws OpsException {
		InputStream dataStream;
		try {
			dataStream = upload.data.getInputStream();
		} catch (IOException e) {
			throw new OpsException("Error opening data stream", e);
		}
		long dataLength = upload.data.getLength();
		try {
			log.info("Uploading file over ssh: " + upload.path);
			sshConnection.sshCopyData(dataStream, dataLength, upload.path.getPath(), upload.mode, needSudo());
		} catch (IOException e) {
			throw new OpsException("Error during file upload", e);
		} catch (InterruptedException e) {
			ExceptionUtils.handleInterrupted(e);
			throw new OpsException("Error during file upload", e);
		} catch (SshException e) {
			throw new OpsException("Error during file upload", e);
		}
	}

	private boolean needSudo() {
		if (ensureRunningAsRoot) {
			if (!sshConnection.getUser().equals("root")) {
				return true;
			}
		}

		return false;
	}

	@Override
	protected ProcessExecution executeCommandUnchecked(Command command) throws ProcessExecutionException {
		try {
			String commandString = command.buildCommandString();
			TimeSpan timeout = command.getTimeout();
			return sshConnection.sshExecute(commandString, timeout);
		} catch (IOException e) {
			throw new ProcessExecutionException("Error during command execution", e);
		} catch (InterruptedException e) {
			ExceptionUtils.handleInterrupted(e);
			throw new ProcessExecutionException("Error during command execution", e);
		} catch (SshException e) {
			throw new ProcessExecutionException("Error during command execution", e);
		}
	}

	@Override
	public String readTextFile(File path) throws OpsException {
		byte[] contents = readBinaryFile(path);
		if (contents == null) {
			return null;
		}
		return Utf8.toString(contents);
	}

	@Override
	public byte[] readBinaryFile(File path) throws OpsException {
		byte[] contents;
		try {
			contents = sshConnection.sshReadFile(path.getPath(), needSudo());
		} catch (IOException e) {
			throw new OpsException("Error reading file", e);
		} catch (InterruptedException e) {
			throw new OpsException("Error reading file", e);
		} catch (SshException e) {
			throw new OpsException("Error reading file", e);
		}
		return contents;
	}

	@Override
	public boolean isSameMachine(OpsTarget compare) {
		if (compare instanceof SshOpsTarget) {
			InetAddress compareHost = ((SshOpsTarget) compare).sshConnection.getHost();
			InetAddress myHost = sshConnection.getHost();

			return Objects.equal(compareHost, myHost);
		}
		return false;
	}

	@Override
	public NetworkPoint getNetworkPoint() {
		InetAddress myHost = sshConnection.getHost();
		return NetworkPoint.forSshAddress(myHost);
	}

	public Socket buildTunneledSocket() throws OpsException {
		try {
			return sshConnection.buildTunneledSocket();
		} catch (IOException e) {
			throw new OpsException("Error setting up SSH port forward", e);
		} catch (SshException e) {
			throw new OpsException("Error setting up SSH port forward", e);
		}
	}

	public SshPortForward forwardLocalPort(InetSocketAddress remoteSocketAddress) throws OpsException {
		try {
			return sshConnection.forwardLocalPort(remoteSocketAddress);
		} catch (IOException e) {
			throw new OpsException("Error setting up SSH port forward", e);
		} catch (SshException e) {
			throw new OpsException("Error setting up SSH port forward", e);
		}
	}

	@Override
	public String toString() {
		return "SshOpsTarget [" + sshConnection.getUser() + "@" + InetAddresses.toAddrString(sshConnection.getHost())
				+ "]";
	}

	@Override
	protected Command maybeSudo(String command) {
		if (needSudo()) {
			return Command.build("sudo " + command);
		} else {
			return Command.build(command);
		}
	}

	@Override
	public boolean isMachineTerminated() {
		return machine.isTerminated();
	}

	public boolean isEnsureRunningAsRoot() {
		return ensureRunningAsRoot;
	}

	public void setEnsureRunningAsRoot(boolean ensureRunningAsRoot) {
		this.ensureRunningAsRoot = ensureRunningAsRoot;
	}
}
