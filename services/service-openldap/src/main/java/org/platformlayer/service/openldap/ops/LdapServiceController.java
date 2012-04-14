package org.platformlayer.service.openldap.ops;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.instances.DiskImageRecipeBuilder;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.metrics.collectd.CollectdCollector;
import org.platformlayer.ops.networks.PublicEndpoint;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.openldap.model.LdapService;
import org.platformlayer.service.openldap.ops.ldap.LdapMasterPassword;

public class LdapServiceController extends OpsTreeBase {
	static final Logger log = Logger.getLogger(LdapServiceController.class);

	@Handler
	public void doOperation() throws OpsException, IOException {
		// TODO: We keep creating dc=nodomain; we should remove it or stop it being created
	}

	@Override
	protected void addChildren() throws OpsException {
		LdapService model = OpsContext.get().getInstance(LdapService.class);

		// TODO: Support package pre-configuration??
		InstanceBuilder instance = InstanceBuilder.build(model.dnsName,
				DiskImageRecipeBuilder.loadDiskImageResource(getClass(), "DiskImageRecipe.xml"));
		addChild(instance);

		instance.addChild(CollectdCollector.build());

		instance.addChild(LdapMasterPassword.build(model.ldapServerPassword));

		{
			PublicEndpoint endpoint = injected(PublicEndpoint.class);
			// endpoint.network = null;
			endpoint.publicPort = 389;
			endpoint.backendPort = 389;
			endpoint.dnsName = model.dnsName;

			endpoint.tagItem = OpsSystem.toKey(model);
			endpoint.parentItem = OpsSystem.toKey(model);

			instance.addChild(endpoint);
		}
	}
}
