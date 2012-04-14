package org.platformlayer.ops.helpers;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.TimeSpan;
import org.platformlayer.core.model.ManagedItemState;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.CommandEnvironment;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.process.ProcessExecutionException;
import org.platformlayer.service.aptcache.v1.AptCacheService;
import org.platformlayer.service.imagefactory.v1.ConfigurePackage;

import com.google.common.collect.Lists;

public class AptHelper {
	static final Logger log = Logger.getLogger(AptHelper.class);

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	OpsContext ops;

	@Inject
	InstanceHelpers instances;

	public List<String> findHttpProxies(OpsTarget target) throws OpsException {
		List<String> proxies = Lists.newArrayList();

		for (AptCacheService aptCacheService : platformLayer.listItems(AptCacheService.class)) {
			if (aptCacheService.getState() != ManagedItemState.ACTIVE) {
				continue;
			}

			{
				// By DNS
				String dnsName = aptCacheService.getDnsName();
				String address = "http://" + dnsName + ":3128/";
				proxies.add(address);
			}

			{
				// By IP
				Machine machine = instances.findMachine(aptCacheService);
				if (machine != null) {
					NetworkPoint targetNetworkPoint = NetworkPoint.forTarget(target);

					String address = "http://" + machine.getAddress(targetNetworkPoint, 3128) + ":3128/";
					proxies.add(address);
				} else {
					log.warn("Could not find machine for apt-cache: " + aptCacheService);
				}
			}
		}

		// TODO: Support grabbing a proxy from the configuration
		// proxies.add("http://192.168.192.40:3142/");

		return proxies;
	}

	CommandEnvironment httpProxyEnvironment = null;

	public CommandEnvironment getHttpProxyEnvironment(OpsTarget target) throws OpsException {
		if (httpProxyEnvironment == null) {
			List<String> proxies = findHttpProxies(target);
			httpProxyEnvironment = new CommandEnvironment();

			String proxy = chooseProxy(target, proxies);
			if (proxy != null) {
				log.info("Will use http proxy: " + proxy);
				httpProxyEnvironment.add("http_proxy", proxy);
			} else {
				log.info("No suitable http proxy found");
			}
		}
		return httpProxyEnvironment;
	}

	private String chooseProxy(OpsTarget target, List<String> proxies) {
		String bestProxy = null;
		TimeSpan bestTime = null;
		for (String proxy : proxies) {
			// {
			// // We choose the fastest proxy that gives us a 200 response
			// String url = proxy + "acng-report.html";
			// CurlRequest request = new CurlRequest(url);
			// request.setTimeout(5);
			// try {
			// CurlResult result = request.executeRequest(target);
			// if (result.getHttpResult() != 200) {
			// log.info("Unexpected response code while testing proxy: " + proxy + ".  Code=" + result.getHttpResult());
			// continue;
			// }
			// TimeSpan timeTotal = result.getTimeTotal();
			// if (bestTime == null || timeTotal.isLessThan(bestTime)) {
			// bestProxy = proxy;
			// bestTime = timeTotal;
			// }
			// } catch (ProcessExecutionException e) {
			// log.info("Error while testing proxy: " + proxy, e);
			// }
			// }

			{
				// We choose the fastest proxy that gives us a 200 response
				String url = "http://ftp.debian.org/debian/dists/stable/Release.gpg";
				CurlRequest request = new CurlRequest(url);
				request.proxy = proxy;
				request.timeout = TimeSpan.FIVE_SECONDS;
				try {
					CurlResult result = request.executeRequest(target);
					if (result.getHttpResult() != 200) {
						log.info("Unexpected response code while testing proxy: " + proxy + ".  Code="
								+ result.getHttpResult());
						continue;
					}
					TimeSpan timeTotal = result.getTimeTotal();
					if (bestTime == null || timeTotal.isLessThan(bestTime)) {
						bestProxy = proxy;
						bestTime = timeTotal;
					}
				} catch (ProcessExecutionException e) {
					log.info("Error while testing proxy: " + proxy, e);
				}
			}

		}
		return bestProxy;
	}

	public void install(OpsTarget target, String... packageNames) throws OpsException {
		CommandEnvironment commandEnvironment = buildEnvironmentWithProxy(target);

		commandEnvironment.add("DEBIAN_FRONTEND", "noninteractive");

		Command command = Command.build("apt-get install --yes");
		for (String packageName : packageNames) {
			command.addQuoted(packageName);
		}
		target.executeCommand(command.setEnvironment(commandEnvironment).setTimeout(TimeSpan.TEN_MINUTES));
	}

	private CommandEnvironment buildEnvironmentWithProxy(OpsTarget target) throws OpsException {
		CommandEnvironment commandEnvironment = null;

		if (!haveCurl(target)) {
			log.warn("We don't yet have curl; can't detect best proxy so won't use a proxy");
		} else {
			commandEnvironment = getHttpProxyEnvironment(target);
		}

		if (commandEnvironment == null) {
			commandEnvironment = new CommandEnvironment();
		}

		return commandEnvironment;
	}

	public void update(OpsTarget target) throws OpsException {
		CommandEnvironment commandEnvironment = buildEnvironmentWithProxy(target);

		Command command = Command.build("apt-get --yes update");
		target.executeCommand(command.setEnvironment(commandEnvironment).setTimeout(TimeSpan.TEN_MINUTES));
	}

	public void upgrade(OpsTarget target) throws OpsException {
		CommandEnvironment commandEnvironment = buildEnvironmentWithProxy(target);

		Command command = Command.build("apt-get --yes upgrade");
		target.executeCommand(command.setEnvironment(commandEnvironment).setTimeout(TimeSpan.TEN_MINUTES));
	}

	private boolean haveCurl(OpsTarget target) throws OpsException {
		return target.getFilesystemInfoFile(new File("/usr/bin/curl")) != null;
	}

	public void addRepositoryKeyUrl(OpsTarget target, String url) throws OpsException {
		Command command = Command.build("wget -q -O - {0} | apt-key add -", url);
		target.executeCommand(command);
	}

	public void addRepository(OpsTarget target, String id, List<String> sources) throws OpsException {
		File dir = new File("/etc/apt/sources.list.d");
		File file = new File(dir, id + ".list");

		StringBuilder sb = new StringBuilder();
		for (String source : sources) {
			sb.append(source);
			sb.append("\n");
		}

		log.info("Uploading to " + file + ": " + sb.toString());

		target.setFileContents(file, sb.toString());
	}

	public void preconfigurePackages(OpsTarget target, List<ConfigurePackage> settings) throws OpsException {
		File preseedTmpDir = target.createTempDir();

		StringBuilder sb = new StringBuilder();

		for (ConfigurePackage conf : settings) {
			String confType = conf.getType();
			if (confType == null) {
				confType = "string";
			}
			String line = conf.getPackageName() + "\t" + conf.getKey() + "\t" + conf.getType() + "\t" + conf.getValue()
					+ "\n";
			sb.append(line);
		}

		File preseedFile = new File(preseedTmpDir, "misc.preseed");

		target.setFileContents(preseedFile, sb.toString());
		target.executeCommand(Command.build("cat {0} | debconf-set-selections", preseedFile));
	}

}
