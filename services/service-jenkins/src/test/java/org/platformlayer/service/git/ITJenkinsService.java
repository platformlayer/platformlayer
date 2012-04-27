package org.platformlayer.service.git;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;

import org.openstack.utils.Io;
import org.platformlayer.service.jenkins.model.JenkinsService;
import org.platformlayer.service.jenkins.ops.JenkinsServiceController;
import org.platformlayer.tests.PlatformLayerApiTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ITJenkinsService extends PlatformLayerApiTest {

	@Override
	@BeforeMethod
	public void beforeMethod() {
		super.beforeMethod();

		getTypedItemMapper().addClass(JenkinsService.class);
	}

	@Test
	public void testCreateAndDeleteItem() throws Exception {
		String id = "jenkins-" + random.randomAlphanumericString(8);

		JenkinsService service = new JenkinsService();
		service.dnsName = id + ".test.platformlayer.org";

		service = putItem(id, service);
		service = waitForHealthy(service);

		InetSocketAddress socketAddress = getEndpoint(service);
		Assert.assertFalse(isPortOpen(socketAddress));

		openFirewall(service, JenkinsServiceController.PORT);
		Assert.assertTrue(isPortOpen(socketAddress));

		// TODO: Make endpoint http://<ip>:<port>/<path>...
		String url = "http://" + socketAddress.getAddress().getHostAddress() + ":" + socketAddress.getPort()
				+ "/api/xml";

		String html = testJenkins(url);
		Assert.assertTrue(html.contains("<numExecutors>"));
	}

	private String testJenkins(String jenkinsUrl) throws IOException {
		String fetchUrl = jenkinsUrl;

		URL url = new URL(fetchUrl);

		String html = Io.readAll(url);
		System.out.println(html);

		return html;
	}

}
