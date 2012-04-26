package org.platformlayer.service.git;

import java.net.InetSocketAddress;

import org.platformlayer.service.git.model.GitRepository;
import org.platformlayer.service.git.model.GitService;
import org.platformlayer.service.git.ops.GitServerController;
import org.platformlayer.tests.PlatformLayerApiTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ITGitService extends PlatformLayerApiTest {

	@BeforeMethod
	public void beforeMethod() {
		reset();

		getTypedItemMapper().addClass(GitService.class);
	}

	@Test
	public void testCreateAndDeleteItem() throws Exception {
		String id = "git" + random.randomAlphanumericString(8);

		GitService service = new GitService();
		service.dnsName = id + ".test.platformlayer.org";
		service.ldapGroup = "";

		service = putItem(id, service);
		service = waitForHealthy(service);

		InetSocketAddress socketAddress = getEndpoint(service);
		Assert.assertFalse(isPortOpen(socketAddress));

		openFirewall(service, GitServerController.PORT);
		Assert.assertTrue(isPortOpen(socketAddress));

		String repoId = "repo" + id;
		GitRepository repo = new GitRepository();
		repo.name = repoId;
		repo = putItem(repoId, repo);
		repo = waitForHealthy(repo);

		// TODO: Make endpoint http://<ip>:<port>/<path>...
		String url = "http://" + socketAddress.getAddress().getHostAddress() + ":" + socketAddress.getPort() + "/git";
		testGitRepo(url);

		deleteItem(repo);
		deleteItem(service);
	}

	private void testGitRepo(String url) {

	}

}
