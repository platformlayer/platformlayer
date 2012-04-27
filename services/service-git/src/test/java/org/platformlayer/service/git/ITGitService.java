package org.platformlayer.service.git;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;

import org.openstack.utils.Io;
import org.platformlayer.service.git.model.GitRepository;
import org.platformlayer.service.git.model.GitService;
import org.platformlayer.service.git.ops.GitServerController;
import org.platformlayer.service.openldap.OpenLdapTestHelpers;
import org.platformlayer.service.openldap.model.LdapDomain;
import org.platformlayer.service.openldap.model.LdapService;
import org.platformlayer.service.openldap.ops.LdapServiceController;
import org.platformlayer.tests.PlatformLayerApiTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ITGitService extends PlatformLayerApiTest {

	@Override
	@BeforeMethod
	public void beforeMethod() {
		super.beforeMethod();

		getTypedItemMapper().addClass(LdapService.class);
		getTypedItemMapper().addClass(LdapDomain.class);

		getTypedItemMapper().addClass(GitService.class);
	}

	@Test
	public void testCreateAndDeleteItem() throws Exception {
		OpenLdapTestHelpers openLdap = new OpenLdapTestHelpers(getContext());
		LdapService ldapService = openLdap.createLdapServer();

		openFirewall(ldapService, LdapServiceController.PORT);

		String organizationName = "test.platformlayer.org";
		LdapDomain ldapDomain = openLdap.createLdapDomain(ldapService, organizationName);

		String id = "git" + random.randomAlphanumericString(8);

		GitService service = new GitService();
		service.dnsName = id + ".test.platformlayer.org";
		service.ldapGroup = "ou=Git Users,dc=test,dc=platformlayer,dc=org";

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
		String url = "http://" + socketAddress.getAddress().getHostAddress() + ":" + socketAddress.getPort() + "/git/"
				+ repoId;
		String username = null;
		String password = null;

		testGitRepo(url, username, password);
	}

	private void testGitRepo(String repoUrl, String username, String password) throws MalformedURLException {
		String fetchUrl = repoUrl;

		URL url = new URL(fetchUrl);

		// TODO: Create an LDAP user
		System.out.println("Warning - not properly checking git repo");

		/*
		 * HttpURLConnection uc = (HttpURLConnection) url.openConnection(); sun.misc.BASE64Encoder enc = new
		 * sun.misc.BASE64Encoder(); String userpassword = username + ":" + password; String encodedAuthorization =
		 * enc.encode(userpassword.getBytes()); uc.setRequestProperty("Authorization", "Basic " + encodedAuthorization);
		 * uc.connect(); InputStream is = uc.getInputStream(); String html = Io.readAll(is); is.close();
		 * System.out.println(html);
		 * 
		 * return html;
		 */

		try {
			Io.readAll(url);
		} catch (Exception e) {
			// We're not authenticated, so we expect a 401

			System.out.println(e);

			String message = e.getMessage();
			Assert.assertTrue(message.contains("HTTP response code: 401"));
		}
	}

}
