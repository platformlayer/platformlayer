package org.platformlayer.ops.helpers;

import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.TimeSpan;
import org.platformlayer.core.model.ManagedItemState;
import org.platformlayer.ops.CommandEnvironment;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.process.ProcessExecutionException;
import org.platformlayer.service.aptcache.v1.AptCacheService;

import com.google.common.collect.Lists;

public class HttpProxyHelper {
	static final Logger log = Logger.getLogger(HttpProxyHelper.class);

	public enum Usage {
		SoftwarePackages, General
	};

	@Inject
	PlatformLayerHelpers platformLayer;

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

	public CommandEnvironment getHttpProxyEnvironment(OpsTarget target, Usage usage) throws OpsException {
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

}
