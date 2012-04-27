package org.platformlayer.service.openldap.tests;

import java.io.IOException;

import org.platformlayer.core.model.Secret;
import org.platformlayer.ops.OpsException;
import org.platformlayer.service.openldap.model.LdapDomain;
import org.platformlayer.service.openldap.model.LdapService;
import org.platformlayer.tests.PlatformLayerTestContext;
import org.platformlayer.tests.TestHelper;

public class OpenLdapTestHelpers extends TestHelper {

	public OpenLdapTestHelpers(PlatformLayerTestContext context) {
		super(context);
	}

	public LdapService createLdapServer() throws OpsException, IOException {
		String id = random.randomAlphanumericString(8);
		Secret ldapServerPassword = randomSecret();

		LdapService service = new LdapService();
		service.dnsName = id + ".test.platformlayer.org";
		service.ldapServerPassword = ldapServerPassword;

		service = context.putItem(id, service);
		service = context.waitForHealthy(service);

		return service;
	}

	public LdapDomain createLdapDomain(LdapService ldapService, String organizationName) throws OpsException,
			IOException {
		String domainId = "domain-" + ldapService.getId();
		Secret adminPassword = randomSecret();
		LdapDomain domain = new LdapDomain();
		domain.organizationName = organizationName;
		domain.adminPassword = adminPassword;
		domain = context.putItem(domainId, domain);
		domain = context.waitForHealthy(domain);

		return domain;
	}

}
