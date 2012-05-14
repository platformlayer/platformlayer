package org.platformlayer.ops.images.direct;

import java.io.File;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.openstack.utils.Hex;
import org.platformlayer.ExceptionUtils;
import org.platformlayer.TimeSpan;
import org.platformlayer.crypto.Md5Hash;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.SshOpsTarget;
import org.platformlayer.ops.networks.IpRange;
import org.platformlayer.ops.process.ProcessExecution;
import org.platformlayer.ops.process.ProcessExecutionException;

import com.google.common.base.CharMatcher;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class PeerToPeerCopy {
	static final Logger log = Logger.getLogger(PeerToPeerCopy.class);

	@Inject
	ExecutorService executorService;

	public void copy(final OpsTarget src, final File srcFile, OpsTarget dest, File destFile) throws OpsException {
		int maxAttempts = 3;
		Random random = new Random();

		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			final InetAddressPair channel = findChannel(src, dest);

			// TODO: Better security
			final int port = random.nextInt(1000) + 20000;

			Callable<ProcessExecution> serveFile = new Callable<ProcessExecution>() {
				@Override
				public ProcessExecution call() throws Exception {
					// TODO: Secure this better (using host address is probably sufficient, but then we need a full
					// network map to know which IP to use)
					// Command sendCommand = Command.build("socat -u OPEN:{0},rdonly TCP4-LISTEN:{1},range={2}",
					// srcImageFile, port, targetAddress.getHostAddress() + "/32");
					Command sendCommand = Command.build("socat -u OPEN:{0},rdonly {1}", srcFile,
							toSocatListen(channel.dest, port));
					return src.executeCommand(sendCommand.setTimeout(TimeSpan.TEN_MINUTES));
				}
			};

			Future<ProcessExecution> serveFuture = executorService.submit(serveFile);

			for (int readAttempts = 1; readAttempts <= 10; readAttempts++) {
				TimeSpan.ONE_SECOND.doSafeSleep();

				Command receiveCommand = Command.build("socat -u {0} CREATE:{1}", toSocatDest(channel.src, port),
						destFile);
				try {
					dest.executeCommand(receiveCommand.setTimeout(TimeSpan.TEN_MINUTES));
					break;
				} catch (ProcessExecutionException e) {
					ProcessExecution recvExecution = e.getExecution();
					if (recvExecution.getExitCode() == 1 && recvExecution.getStdErr().contains("Connection refused")) {
						log.info("Got connection refused; will retry");
					} else {
						throw new OpsException("Error receiving image file", e);
					}
				}

				if (serveFuture.isDone()) {
					try {
						ProcessExecution serveExecution = serveFuture.get();
						log.warn("Image sending exited: " + serveExecution);
					} catch (ExecutionException e) {
						throw new OpsException("Error sending file to image store", e);
					} catch (InterruptedException e) {
						ExceptionUtils.handleInterrupted(e);
						throw new OpsException("Error sending file to image store", e);
					}
				}
			}

			Md5Hash targetHash = dest.getFileHash(destFile);
			Md5Hash srcHash = src.getFileHash(srcFile);

			if (Objects.equal(srcHash, targetHash)) {
				break;
			} else {
				dest.rm(destFile);
				if (attempt != maxAttempts) {
					log.warn("Files did not match after transfer");
				} else {
					throw new OpsException("Files did not match after transfer");
				}
			}

			if (serveFuture.isDone()) {
				// This is interesting for debug purposes; otherwise not very useful
				try {
					ProcessExecution serveExecution = serveFuture.get();
					log.warn("Serving process exited: " + serveExecution);
				} catch (ExecutionException e) {
					throw new OpsException("Error sending file to image store", e);
				} catch (InterruptedException e) {
					ExceptionUtils.handleInterrupted(e);
					throw new OpsException("Error sending file to image store", e);
				}
			}
		}
	}

	private InetAddressPair findChannel(OpsTarget src, OpsTarget target) throws OpsException {
		InetAddress srcAddress = ((SshOpsTarget) src).getHost();
		InetAddress targetAddress = ((SshOpsTarget) target).getHost();

		if (srcAddress instanceof Inet4Address) {
			if (targetAddress instanceof Inet6Address) {
				Inet6Address srcIpv6 = findIpv6(src);
				if (srcIpv6 != null) {
					srcAddress = srcIpv6;
				} else {
					throw new UnsupportedOperationException();
				}
			}
		} else {
			if (targetAddress instanceof Inet4Address) {
				Inet6Address targetIpv6 = findIpv6(target);
				if (targetIpv6 != null) {
					targetAddress = targetIpv6;
				} else {
					throw new UnsupportedOperationException();
				}
			}
		}

		return new InetAddressPair(srcAddress, targetAddress);
	}

	private Inet6Address findIpv6(OpsTarget target) throws OpsException {
		Command command = Command.build("cat /proc/net/if_inet6");
		ProcessExecution execution = target.executeCommand(command);
		String inet6 = execution.getStdOut();

		// This didn't work for some reason (??)
		// String inet6 = target.readTextFile(new File("/proc/net/if_inet6"));

		List<Inet6Address> addresses = Lists.newArrayList();

		for (String line : Splitter.on('\n').split(inet6)) {
			line = line.trim();
			if (line.isEmpty()) {
				continue;
			}

			List<String> tokens = Lists
					.newArrayList(Splitter.on(CharMatcher.WHITESPACE).omitEmptyStrings().split(line));
			if (tokens.size() != 6) {
				throw new IllegalStateException("Cannot parse ipv6 address line: " + line);
			}

			String addressString = tokens.get(0);

			byte[] addr = Hex.fromHex(addressString);

			Inet6Address address;
			try {
				address = (Inet6Address) InetAddress.getByAddress(addr);
			} catch (UnknownHostException e) {
				throw new IllegalStateException("Error parsing IP address: " + line);
			}
			addresses.add(address);
		}

		IpRange publicIpv6 = IpRange.parse("2000::/3");
		for (Inet6Address address : addresses) {
			if (publicIpv6.isInRange(address)) {
				return address;
			}
		}

		return null;
	}

	private String toSocatDest(InetAddress srcAddress, int port) {
		if (srcAddress instanceof Inet4Address) {
			return "TCP4:" + srcAddress.getHostAddress() + ":" + port;
		} else if (srcAddress instanceof Inet6Address) {
			return "TCP6:[" + srcAddress.getHostAddress() + "]:" + port;
		} else {
			throw new IllegalStateException();
		}
	}

	protected String toSocatListen(InetAddress address, int port) {
		if (address instanceof Inet4Address) {
			return "TCP4-LISTEN:" + port; // + ",bind=" + address.getHostAddress();
		} else if (address instanceof Inet6Address) {
			return "TCP6-LISTEN:" + port; // + ",bind=" + address.getHostAddress();
		} else {
			throw new IllegalStateException();
		}
	}
}
