package org.platformlayer.service.aptcache;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

import org.openstack.utils.Io;
import org.platformlayer.service.aptcache.model.AptCacheService;
import org.platformlayer.service.aptcache.ops.AptCacheServiceController;
import org.platformlayer.tests.PlatformLayerApiTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ITAptCacheService extends PlatformLayerApiTest {

	@Override
	@BeforeMethod
	public void beforeMethod() {
		super.beforeMethod();

		getTypedItemMapper().addClass(AptCacheService.class);
	}

	@Test
	public void testCreateAndDeleteItem() throws Exception {
		String id = random.randomAlphanumericString(8);

		AptCacheService aptCache = new AptCacheService();
		aptCache.dnsName = id + ".test.platformlayer.org";

		aptCache = putItem(id, aptCache);
		aptCache = waitForHealthy(aptCache);

		InetSocketAddress socketAddress = getEndpoint(aptCache);
		Assert.assertFalse(isPortOpen(socketAddress));

		openFirewall(aptCache, AptCacheServiceController.PORT);
		Assert.assertTrue(isPortOpen(socketAddress));

		// TODO: Make endpoint http://<ip>:<port>/ ???
		// String httpUrl = "http://" + socketAddress.getAddress().getHostAddress() + ":" + socketAddress.getPort();
		String html = testProxy(socketAddress, "http://www.google.com/");
		Assert.assertTrue(html.contains("Search the world"));

		deleteItem(aptCache);
	}

	private String testProxy(InetSocketAddress proxySocketAddress, String fetchUrl) throws IOException {
		Proxy proxy = new Proxy(Proxy.Type.HTTP, proxySocketAddress);
		URL url = new URL(fetchUrl);
		HttpURLConnection uc = (HttpURLConnection) url.openConnection(proxy);
		uc.connect();

		InputStream is = uc.getInputStream();
		String html = Io.readAll(is);
		is.close();

		System.out.println(html);

		return html;
	}

}
