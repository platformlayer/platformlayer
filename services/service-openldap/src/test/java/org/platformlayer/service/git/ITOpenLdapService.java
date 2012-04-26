package org.platformlayer.service.git;

import java.net.InetSocketAddress;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.platformlayer.core.model.Secret;
import org.platformlayer.service.openldap.model.LdapDomain;
import org.platformlayer.service.openldap.model.LdapService;
import org.platformlayer.service.openldap.ops.LdapServiceController;
import org.platformlayer.tests.PlatformLayerApiTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ITOpenLdapService extends PlatformLayerApiTest {

	@BeforeMethod
	public void beforeMethod() {
		reset();

		getTypedItemMapper().addClass(LdapService.class);
		getTypedItemMapper().addClass(LdapDomain.class);
	}

	@Test
	public void testCreateAndDeleteItem() throws Exception {
		String id = random.randomAlphanumericString(8);
		Secret ldapServerPassword = randomSecret();

		LdapService service = new LdapService();
		service.dnsName = id + ".test.platformlayer.org";
		service.ldapServerPassword = ldapServerPassword;

		service = putItem(id, service);
		service = waitForHealthy(service);

		InetSocketAddress socketAddress = getEndpoint(service);
		Assert.assertFalse(isPortOpen(socketAddress));

		openFirewall(service, LdapServiceController.PORT);
		Assert.assertTrue(isPortOpen(socketAddress));

		String organizationName = "test.platformlayer.org";

		String domainId = "domain-" + id;
		Secret adminPassword = randomSecret();
		LdapDomain domain = new LdapDomain();
		domain.organizationName = organizationName;
		domain.adminPassword = adminPassword;
		domain = putItem(domainId, domain);
		domain = waitForHealthy(domain);

		// TODO: Make endpoint ldap://<ip>:<port>/ ???
		String ldapUrl = "ldap://" + socketAddress.getAddress().getHostAddress() + ":" + socketAddress.getPort() + "/";
		testLdap(ldapUrl, adminPassword);

		deleteItem(domain);
		deleteItem(service);
	}

	private void testLdap(String ldapUrl, Secret adminPassword) throws NamingException {
		Hashtable<String, String> env = new Hashtable<String, String>();

		String sp = "com.sun.jndi.ldap.LdapCtxFactory";
		env.put(Context.INITIAL_CONTEXT_FACTORY, sp);

		env.put(Context.PROVIDER_URL, ldapUrl);

		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, "cn=Manager,dc=test,dc=platformlayer,dc=org");
		env.put(Context.SECURITY_CREDENTIALS, adminPassword.plaintext());

		DirContext ctx = new InitialDirContext(env);

		NamingEnumeration results = ctx.list("dc=test,dc=platformlayer,dc=org");
		while (results.hasMore()) {
			NameClassPair sr = (NameClassPair) results.next();
			System.out.println(sr.getNameInNamespace());
		}

		ctx.close();
	}

}
