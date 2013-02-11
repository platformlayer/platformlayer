package org.platformlayer.ops.images.direct;

import java.io.File;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import org.platformlayer.ExceptionUtils;
import org.platformlayer.TimeSpan;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.SshOpsTarget;
import org.platformlayer.ops.firewall.Protocol;
import org.platformlayer.ops.firewall.Transport;
import org.platformlayer.ops.firewall.scripts.IptablesFilterEntry;
import org.platformlayer.ops.networks.IpRange;
import org.platformlayer.ops.process.ProcessExecution;
import org.platformlayer.ops.process.ProcessExecutionException;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.hash.Md5Hash;
import com.fathomdb.utils.Hex;
import com.google.common.base.CharMatcher;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class SocatPeerToPeerCopy implements PeerToPeerCopy {
	public static final int PORT = 1201;

	public static class FirewallRules extends OpsTreeBase {
		@SuppressWarnings("unused")
		private static final Logger log = LoggerFactory.getLogger(FirewallRules.class);

		private static final String KEY = "PeerToPeerCopy";

		@Handler
		public void handler() {
		}

		@Override
		protected void addChildren() throws OpsException {
			IptablesFilterEntry entry = addChild(IptablesFilterEntry.class);
			entry.ruleKey = KEY;
			entry.port = PORT;
			entry.transport = Transport.Ipv6;
			entry.protocol = Protocol.Tcp;
		}
	}

	static final Logger log = LoggerFactory.getLogger(SocatPeerToPeerCopy.class);

	@Inject
	ExecutorService executorService;

	@Override
	public void copy(final OpsTarget src, final File srcFile, final OpsTarget dest, final File finalDestFile)
			throws OpsException {
		File tempDir = dest.createTempDir();

		try {
			final File tempDest = new File(tempDir, finalDestFile.getName());

			int maxAttempts = 3;

			for (int attempt = 1; attempt <= maxAttempts; attempt++) {
				final InetAddressPair channel = findChannel(src, dest);

				final int listenPort = PORT;

				Callable<ProcessExecution> pushFile = new Callable<ProcessExecution>() {
					@Override
					public ProcessExecution call() throws Exception {
						// TODO: This is actually _really_ problematic because pkill kills proceses in guests also...
						// // TODO: Check no concurrent socats?? Move to a better system??
						// Command killExistingSocats = Command.build("pkill socat || true");
						// src.executeCommand(killExistingSocats);

						// TODO: Secure this better (using host address is probably sufficient, but then we need a full
						// network map to know which IP to use)
						// Command sendCommand = Command.build("socat -u OPEN:{0},rdonly TCP4-LISTEN:{1},range={2}",
						// srcImageFile, port, targetAddress.getHostAddress() + "/32");
						Command pushCommand = Command.build("socat -u OPEN:{0},rdonly {1}", srcFile,
								toSocatPush(channel.dest, listenPort));
						return src.executeCommand(pushCommand.setTimeout(TimeSpan.TEN_MINUTES));
					}
				};

				Callable<ProcessExecution> listenFile = new Callable<ProcessExecution>() {
					@Override
					public ProcessExecution call() throws Exception {
						// TODO: This is actually _really_ problematic because pkill kills proceses in guests also...
						// TODO: Check no concurrent socats?? Move to a better system??
						Command killExistingSocats = Command.build("pkill socat || true");
						dest.executeCommand(killExistingSocats);

						try {
							Command listenCommand = Command.build("socat -u {0} CREATE:{1}",
									toSocatListen(channel.src, listenPort), tempDest);
							return dest.executeCommand(listenCommand.setTimeout(TimeSpan.TEN_MINUTES));
						} catch (ProcessExecutionException e) {
							log.warn("Error running listen process for P2P copy: " + channel, e);
							throw new OpsException("Error running listen process", e);
						}
					}
				};

				Future<ProcessExecution> listenFileFuture = executorService.submit(listenFile);

				for (int readAttempts = 1; readAttempts <= 10; readAttempts++) {
					TimeSpan.ONE_SECOND.doSafeSleep();

					if (!listenFileFuture.isDone()) {
						boolean pushedOkay = false;
						try {
							pushFile.call();
							pushedOkay = true;
						} catch (Exception e) {
							ProcessExecution pushExecution = null;
							if (e instanceof ProcessExecutionException) {
								pushExecution = ((ProcessExecutionException) e).getExecution();
							}

							if (pushExecution != null && pushExecution.getExitCode() == 1
									&& pushExecution.getStdErr().contains("Connection refused")) {
								log.info("Got connection refused on push; will retry");
							} else {
								throw new OpsException("Error pushing file", e);
							}
						}

						if (pushedOkay) {
							try {
								listenFileFuture.get(5, TimeUnit.SECONDS);
							} catch (TimeoutException e) {
								log.info("Timeout while waiting for receive to complete");
							} catch (InterruptedException e) {
								Thread.currentThread().interrupt();
								throw new OpsException("Interrupted during file receive", e);
							} catch (ExecutionException e) {
								throw new OpsException("Error during file receive", e);
							}
						}
					}

					if (listenFileFuture.isDone()) {
						try {
							ProcessExecution listenExecution = listenFileFuture.get();
							log.warn("File receiving exited: " + listenExecution);
							break;
						} catch (ExecutionException e) {
							throw new OpsException("Error receiving file", e);
						} catch (InterruptedException e) {
							ExceptionUtils.handleInterrupted(e);
							throw new OpsException("Error receiving file", e);
						}
					}
				}

				if (!listenFileFuture.isDone()) {
					throw new OpsException("Failed to push file (too many retries");
				}

				Md5Hash targetHash = dest.getFileHash(tempDest);
				Md5Hash srcHash = src.getFileHash(srcFile);

				if (Objects.equal(srcHash, targetHash)) {
					break;
				} else {
					dest.rm(tempDest);
					if (attempt != maxAttempts) {
						log.warn("Files did not match after transfer");
					} else {
						throw new OpsException("Files did not match after transfer");
					}
				}

				// if (serveFuture.isDone()) {
				// // This is interesting for debug purposes; otherwise not very useful
				// try {
				// ProcessExecution serveExecution = serveFuture.get();
				// log.warn("Serving process exited: " + serveExecution);
				// } catch (ExecutionException e) {
				// throw new OpsException("Error sending file to image store", e);
				// } catch (InterruptedException e) {
				// ExceptionUtils.handleInterrupted(e);
				// throw new OpsException("Error sending file to image store", e);
				// }
				// }
			}

			dest.mv(tempDest, finalDestFile);
		} finally {
			dest.rmdir(tempDir);
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

	private String toSocatPush(InetAddress destAddress, int port) {
		if (destAddress instanceof Inet4Address) {
			return "TCP4:" + destAddress.getHostAddress() + ":" + port;
		} else if (destAddress instanceof Inet6Address) {
			return "TCP6:[" + destAddress.getHostAddress() + "]:" + port;
		} else {
			throw new IllegalStateException();
		}
	}

	protected String toSocatListen(InetAddress listenAddress, int port) {
		if (listenAddress instanceof Inet4Address) {
			return "TCP4-LISTEN:" + port; // + ",bind=" + address.getHostAddress();
		} else if (listenAddress instanceof Inet6Address) {
			return "TCP6-LISTEN:" + port; // + ",bind=" + address.getHostAddress();
		} else {
			throw new IllegalStateException();
		}
	}
}
