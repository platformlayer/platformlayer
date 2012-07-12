package org.platformlayer.ssh.mina;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.apache.log4j.Logger;
import org.apache.sshd.ClientChannel;
import org.apache.sshd.ClientSession;
import org.openstack.utils.Io;
import org.openstack.utils.Utf8;
import org.platformlayer.ExceptionUtils;
import org.platformlayer.IoUtils;
import org.platformlayer.TimeSpan;
import org.platformlayer.ops.ssh.SshException;
import org.platformlayer.ssh.mina.bugfix.BugFixChannelExec;

/**
 * See http://blogs.sun.com/janp/entry/how_the_scp_protocol_works
 * 
 * @author justinsb
 * 
 */
public class MinaScpClient {
	static final Logger log = Logger.getLogger(MinaScpClient.class);

	private final ClientSession clientSession;

	TimeSpan connectTimeout = TimeSpan.ONE_MINUTE;

	public MinaScpClient(ClientSession clientSession) {
		this.clientSession = clientSession;
	}

	public MinaScpClient(MinaSshConnectionWrapper minaSshConnectionWrapper) {
		this(minaSshConnectionWrapper.sshClientSession);
	}

	public void get(String remoteFile, OutputStream outputStream, TimeSpan timeout, boolean sudo) throws IOException,
			SshException {
		String cmd;

		{
			String trimmed = remoteFile.trim();
			if (trimmed.length() == 0) {
				throw new IllegalArgumentException();
			}
			if (trimmed.contains(" ")) {
				throw new IllegalArgumentException("Spaces in filenames not supported");
			}

			cmd = (sudo ? "sudo " : "") + "scp -f " + trimmed;
		}

		ScpChannel channel = null;
		try {
			channel = new ScpChannel(clientSession, cmd);

			channel.connect(connectTimeout);
			channel.receiveFile(outputStream, timeout);
		} catch (IOException e) {
			throw (IOException) new IOException("Error during SCP download").initCause(e);
		} finally {
			Io.safeClose(channel);
		}
	}

	public void put(InputStream sourceData, long sourceDataLength, String remoteFileName, String remoteDirectory,
			String mode, TimeSpan timeout, boolean sudo) throws IOException, SshException {
		String cmd;

		{
			remoteDirectory = remoteDirectory.trim();
			if (remoteDirectory.length() == 0) {
				throw new IllegalArgumentException();
			}
			if (remoteDirectory.contains(" ")) {
				throw new IllegalArgumentException("Spaces in filenames not supported");
			}

			cmd = (sudo ? "sudo " : "") + "scp -t -d " + remoteDirectory;
		}

		ScpChannel channel = null;
		try {
			channel = new ScpChannel(clientSession, cmd);

			channel.connect(connectTimeout);
			channel.sendFile(sourceData, sourceDataLength, mode, remoteFileName, timeout);
		} catch (IOException e) {
			throw (IOException) new IOException("Error during SCP upload").initCause(e);
		} finally {
			Io.safeClose(channel);
		}
	}

	static class ServerLine {
		final int lineType;
		final String data;

		public ServerLine(int lineType, String data) {
			super();
			this.lineType = lineType;
			this.data = data;
		}

		@Override
		public String toString() {
			return "ServerLine [lineType=" + lineType + ", data=" + data + "]";
		}
	}

	static class FileInfo {
		final String mode;
		final long length;
		final String filename;

		public FileInfo(String mode, long length, String filename) {
			super();
			this.mode = mode;
			this.length = length;
			this.filename = filename;
		}
	}

	static class ScpChannel implements Closeable {
		ClientChannel channel = null;
		PipedOutputStream toStdin;
		PipedInputStream fromStdout;
		ByteArrayOutputStream stderr;

		public ScpChannel(ClientSession clientSession, String cmd) throws IOException {
			this.toStdin = new PipedOutputStream();
			this.fromStdout = new PipedInputStream();

			log.debug("SCP Opening channel for cmd: " + cmd);

			try {
				channel = BugFixChannelExec.createExecChannel(clientSession, cmd);
			} catch (Exception e1) {
				throw new IOException("Cannot create channel", e1);
			}

			this.stderr = new ByteArrayOutputStream();

			channel.setIn(new PipedInputStream(toStdin));
			channel.setOut(new PipedOutputStream(fromStdout));
			channel.setErr(stderr);
		}

		public void connect(TimeSpan connectTimeout) throws SshException {
			try {
				log.debug("SCP Opening connection");
				channel.open().await(connectTimeout.getTotalMilliseconds());
			} catch (Exception e) {
				ExceptionUtils.handleInterrupted(e);
				throw new SshException("SSH error opening channel", e);
			}
		}

		private void receiveFile(OutputStream destOutputStream, TimeSpan timeout) throws IOException, SshException {
			if (timeout == null) {
				timeout = TimeSpan.ZERO;
			}

			byte[] buffer = new byte[8192];

			toStdin.write(0x0);
			toStdin.flush();

			FileInfo fileInfo;

			while (true) {
				ServerLine nextLine = readResponseLine();

				int lineType = nextLine.lineType;

				if (lineType == 1 || lineType == 2) {
					parseFinalLine(nextLine);
					return;
				}

				if (lineType == 'T') {
					// T<mtime> 0 <atime> 0
					// modification and access times when -p options is used (I guess you know why it doesn't make sense
					// to
					// transfer ctime). Times are in seconds, since 00:00:00 UTC, Jan. 1, 1970. Two
					// zeroes are present there in case there is any need to use microseconds in the future. This
					// message
					// was not present in original rcp implementation. Example: T1183828267 0 1183828267 0

					// Ignore
					continue;
				}

				if (lineType == 'C') {
					// Cmmmm <length> <filename>
					// a single file copy, mmmmm is mode. Example: C0644 299 group

					String[] tokens = nextLine.data.split(" ");
					if (tokens.length != 3) {
						throw new SshException("Invalid SCP line: " + nextLine);
					}
					fileInfo = new FileInfo(tokens[0].substring(1), Long.parseLong(tokens[1]), tokens[2]);
					break;
				}
				throw new SshException("Unexpected SCP status: " + nextLine);
			}

			toStdin.write(0x0);
			toStdin.flush();

			try {
				long remain = fileInfo.length;

				while (remain > 0) {
					int readSize = (int) Math.min(buffer.length, remain);

					int actuallyRead = fromStdout.read(buffer, 0, readSize);

					if (actuallyRead < 0) {
						throw new EOFException();
					}

					destOutputStream.write(buffer, 0, actuallyRead);

					remain -= actuallyRead;
				}
			} finally {
				IoUtils.safeClose(destOutputStream);
			}

			parseFinalLine(readResponseLine());

			toStdin.write(0x0);
			toStdin.flush();

			// Wait for everything to finish
			int flags = channel.waitFor(ClientChannel.EOF, timeout.getTotalMilliseconds());
			if ((flags & ClientChannel.TIMEOUT) != 0) {
				throw new SshException("Timeout while waiting for SSH task to complete");
			}
		}

		private void sendFile(InputStream srcData, long srcDataLength, String mode, String remoteFileName,
				TimeSpan timeout) throws IOException, SshException {
			if (timeout == null) {
				timeout = TimeSpan.ZERO;
			}

			byte[] buffer = new byte[32768];

			// It's not entirely clear why the server responds here .. maybe to verify the target directory exists??
			readResponse();

			String line = "C" + mode + " " + srcDataLength + " " + remoteFileName;
			writeCommand(line);

			readResponse();

			long remain = srcDataLength;

			while (remain > 0) {
				int readSize = (int) Math.min(buffer.length, remain);

				int actuallyRead = srcData.read(buffer, 0, readSize);

				if (actuallyRead < 0) {
					throw new EOFException();
				}

				toStdin.write(buffer, 0, actuallyRead);

				remain -= actuallyRead;
			}

			toStdin.write(0);
			toStdin.flush();

			readResponse();

			writeCommand("E");

			readResponse();
		}

		private void writeCommand(String line) throws IOException {
			log.debug("SCP send: " + line);

			line = line + "\n";

			toStdin.write(Utf8.getBytes(line));
			toStdin.flush();
		}

		private ServerLine readResponse() throws IOException, SshException {
			ServerLine nextLine = readResponseLine();

			int lineType = nextLine.lineType;

			if (lineType == 1 || lineType == 2) {
				parseFinalLine(nextLine);
				return nextLine;
			}

			if (lineType != 0) {
				throw new SshException("Unexpected reply: " + lineType);
			}

			return nextLine;
		}

		private ServerLine readResponseLine() throws IOException {
			log.debug("SCP reading response");

			StringBuffer sb = new StringBuffer();

			int lineType = fromStdout.read();
			if (lineType < 0) {
				throw new EOFException();
			}
			if (lineType == 0 || lineType == 2) {
				return new ServerLine(lineType, null);
			}

			while (true) {
				int c = fromStdout.read();

				if (c < 0) {
					throw new EOFException();
				}

				if (c == '\n') {
					break;
				}

				sb.append((char) c);
			}

			ServerLine serverLine = new ServerLine(lineType, sb.toString());
			log.debug("SCP read: [" + serverLine.lineType + "] " + serverLine.data);
			return serverLine;
		}

		private void parseFinalLine(ServerLine line) throws IOException, SshException {
			if (line.lineType == 0) {
				return;
			}

			if ((line.lineType != 1) && (line.lineType != 2)) {
				throw new SshException("Unexpected code found from SCP: " + line);
			}

			if (line.lineType == 2) {
				throw new SshException("SCP error (with no specific message)");
			}

			throw new SshException("SCP error: " + line.data);
		}

		@Override
		public void close() throws IOException {
			IoUtils.safeClose(fromStdout);
			// IoUtils.safeClose(toStdin);

			if (channel != null) {
				channel.close(false);
			}

		}
	}

}
