package org.platformlayer.service.git.ops;

import java.io.File;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.EndpointInfo;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.filesystem.ManagedDirectory;
import org.platformlayer.ops.filesystem.TemplatedFile;
import org.platformlayer.ops.instances.DiskImageRecipeBuilder;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.ldap.LdapDN;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.metrics.collectd.CollectdCollector;
import org.platformlayer.ops.metrics.collectd.ManagedService;
import org.platformlayer.ops.networks.PublicEndpoint;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.ops.templates.TemplateDataSource;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.git.model.GitService;
import org.platformlayer.service.openldap.model.LdapDomain;
import org.platformlayer.service.openldap.model.LdapService;

public class GitServerController extends OpsTreeBase implements TemplateDataSource {
	static final Logger log = Logger.getLogger(GitServerController.class);

	public static final int PORT = 80;

	@Handler
	public void doOperation() {
	}

	@Inject
	PlatformLayerHelpers platformLayer;

	LdapDomain ldapDomain;

	LdapDomain getLdapDomain() throws OpsException {
		if (ldapDomain == null) {
			GitService model = OpsContext.get().getInstance(GitService.class);
			// String ldapGroup = model.ldapGroup;

			LdapDomain best = null;
			for (LdapDomain candidate : platformLayer.listItems(LdapDomain.class)) {
				switch (candidate.getState()) {
				case DELETE_REQUESTED:
				case DELETED:
					continue;
				}

				// String orgName = candidate.organizationName;
				// TODO: How to match?
				if (best == null) {
					best = candidate;
					continue;
				}
				throw new UnsupportedOperationException("Selecting between LDAP domains not yet implemented");
			}

			if (best == null) {
				throw new IllegalStateException("Cannot find LDAP domain: " + model.ldapGroup);
			}
			ldapDomain = best;
		}
		return ldapDomain;
	}

	LdapService ldapService;

	LdapService getLdapService() throws OpsException {
		if (ldapService == null) {
			GitService model = OpsContext.get().getInstance(GitService.class);
			// String ldapGroup = model.ldapGroup;

			LdapService best = null;
			for (LdapService candidate : platformLayer.listItems(LdapService.class)) {
				switch (candidate.getState()) {
				case DELETE_REQUESTED:
				case DELETED:
					continue;
				}

				// TODO: How to match?
				if (best == null) {
					best = candidate;
					continue;
				}
				throw new UnsupportedOperationException("Selecting between LDAP services not yet implemented");
			}

			if (best == null) {
				throw new IllegalStateException("Cannot find LDAP service: " + model.ldapGroup);
			}
			ldapService = best;
		}
		return ldapService;
	}

	@Override
	protected void addChildren() throws OpsException {
		GitService model = OpsContext.get().getInstance(GitService.class);

		InstanceBuilder vm = InstanceBuilder.build(model.dnsName, DiskImageRecipeBuilder.buildDiskImageRecipe(this));
		addChild(vm);

		vm.addChild(PackageDependency.build("apache2"));
		// Provides /usr/lib/git-core/git-http-backend
		vm.addChild(PackageDependency.build("git"));

		vm.addChild(ManagedDirectory.build(new File("/var/git"), "0755"));
		vm.addChild(ApacheModule.build("authnz_ldap"));
		vm.addChild(ApacheModule.build("ldap"));

		File apache2ConfDir = new File("/etc/apache2");

		vm.addChild(TemplatedFile.build(this, new File(apache2ConfDir, "conf.d/git")));

		vm.addChild(ManagedService.build("apache2"));

		vm.addChild(CollectdCollector.build());

		{
			PublicEndpoint endpoint = injected(PublicEndpoint.class);
			// endpoint.network = null;
			endpoint.publicPort = PORT;
			endpoint.backendPort = PORT;
			endpoint.dnsName = model.dnsName;

			endpoint.tagItem = OpsSystem.toKey(model);
			endpoint.parentItem = OpsSystem.toKey(model);

			vm.addChild(endpoint);
		}
	}

	@Override
	public void buildTemplateModel(Map<String, Object> model) throws OpsException {
		LdapDomain ldapDomain = getLdapDomain();
		LdapService ldapService = getLdapService();

		LdapDN organizationDN = LdapDN.fromDomainName(ldapDomain.organizationName);
		LdapDN allUsersDN = organizationDN.childDN("ou", "Users");
		LdapDN managerDN = organizationDN.childDN("cn", "Manager");
		LdapDN groupsDN = organizationDN.childDN("ou", "Groups");
		LdapDN gitUsersDN = groupsDN.childDN("cn", "Git");

		// String authLdapUrl = "ldap://192.168.192.67:389/ou=Users,dc=com,dc=fathomscale?uid";
		// String authLDAPBindDN = "cn=Manager,dc=com,dc=fathomscale";
		// String authLDAPBindPassword = "adminsecret";
		// String requireLdapGroup = "cn=Git,ou=Groups,dc=com,dc=fathomscale";

		int port = 389;
		EndpointInfo ldapEndpoint = EndpointInfo.findEndpoint(ldapService.getTags(), port);
		if (ldapEndpoint == null) {
			throw new OpsException("Cannot find suitable LDAP endpoint");
		}

		// TODO: Maybe we should just reference an LdapGroup

		// TODO: It sucks that we're logging in here as the Manager account

		// LdapGroup -> LdapDomain -> LdapService

		String authLdapUrl = "ldap://" + ldapEndpoint.publicIp + ":389/" + allUsersDN.toLdifEncoded() + "?uid";
		String authLDAPBindDN = managerDN.toLdifEncoded();
		String authLDAPBindPassword = ldapDomain.adminPassword.plaintext();
		String requireLdapGroup = gitUsersDN.toLdifEncoded();

		model.put("AuthLDAPURL", authLdapUrl);
		model.put("AuthLDAPBindDN", authLDAPBindDN);
		model.put("AuthLDAPBindPassword", authLDAPBindPassword);
		model.put("requireLdapGroup", requireLdapGroup);
	}
}
