package org.platformlayer.ops.proxy;

import java.net.URI;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.TimeSpan;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.ManagedItemState;
import org.platformlayer.ops.CommandEnvironment;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.helpers.CurlRequest;
import org.platformlayer.ops.helpers.CurlResult;
import org.platformlayer.ops.helpers.ProviderHelper;
import org.platformlayer.ops.helpers.ProviderHelper.ProviderOf;
import org.platformlayer.ops.networks.NetworkPoint;

import com.google.common.collect.Lists;

public class HttpProxyHelper {
	static final Logger log = Logger.getLogger(HttpProxyHelper.class);

	public enum Usage {
		SoftwarePackages, General
	};

	@Inject
	ProviderHelper providerHelper;

	public List<String> findHttpProxies(OpsTarget target, URI uri) throws OpsException {
		List<String> proxies = Lists.newArrayList();

		for (ProviderOf<HttpProxyController> httpProxyProvider : providerHelper
				.listItemsProviding(HttpProxyController.class)) {
			ItemBase item = httpProxyProvider.getItem();

			if (item.getState() != ManagedItemState.ACTIVE) {
				continue;
			}

			HttpProxyController httpProxy = httpProxyProvider.get();

			NetworkPoint forNetworkPoint = NetworkPoint.forTarget(target);
			String url = httpProxy.getUrl(httpProxyProvider.getItem(), forNetworkPoint, uri);
			if (url == null) {
				log.info("Could not get URL for proxy: " + item);
			} else {
				proxies.add(url);
			}
		}

		// TODO: Support grabbing a proxy from the configuration
		// proxies.add("http://192.168.192.40:3142/");

		return proxies;
	}

	CommandEnvironment httpProxyEnvironment = null;

	public CommandEnvironment getHttpProxyEnvironment(OpsTarget target, Usage usage, URI uri) throws OpsException {
		if (httpProxyEnvironment == null) {
			List<String> proxies = findHttpProxies(target, uri);
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
				} catch (OpsException e) {
					log.info("Error while testing proxy: " + proxy, e);
				}
			}

		}
		return bestProxy;
	}

}
