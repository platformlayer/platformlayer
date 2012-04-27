package org.platformlayer.service.openldap;

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
import org.platformlayer.service.openldap.tests.OpenLdapTestHelpers;
import org.platformlayer.tests.PlatformLayerApiTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ITOpenLdapService extends PlatformLayerApiTest {

	@Override
	@BeforeMethod
	public void beforeMethod() {
		super.beforeMethod();

		getTypedItemMapper().addClass(LdapService.class);
		getTypedItemMapper().addClass(LdapDomain.class);
	}

	@Test
	public void testCreateAndDeleteItem() throws Exception {
		OpenLdapTestHelpers openLdap = new OpenLdapTestHelpers(getContext());
		LdapService ldapService = openLdap.createLdapServer();

		InetSocketAddress socketAddress = getUniqueEndpoint(ldapService);
		Assert.assertFalse(isPortOpen(socketAddress));

		openFirewall(ldapService, LdapServiceController.PORT);
		Assert.assertTrue(isPortOpen(socketAddress));

		String organizationName = "test.platformlayer.org";
		LdapDomain ldapDomain = openLdap.createLdapDomain(ldapService, organizationName);

		// TODO: Make endpoint ldap://<ip>:<port>/ ???
		String ldapUrl = "ldap://" + socketAddress.getAddress().getHostAddress() + ":" + socketAddress.getPort() + "/";
		testLdap(ldapUrl, ldapDomain.adminPassword);
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
