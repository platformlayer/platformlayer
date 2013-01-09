package org.platformlayer.service.wordpress.ops;

import java.io.IOException;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.instances.DiskImageRecipeBuilder;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.networks.PublicEndpoint;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.ops.service.ManagedService;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.network.ops.PlatformLayerFirewallEntry;
import org.platformlayer.service.wordpress.model.WordpressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WordpressServiceController extends OpsTreeBase {

	private static final Logger log = LoggerFactory.getLogger(WordpressServiceController.class);

	@Handler
	public void doOperation() throws OpsException, IOException {
	}

	@Override
	protected void addChildren() throws OpsException {
		WordpressService model = OpsContext.get().getInstance(WordpressService.class);

		InstanceBuilder instance = InstanceBuilder.build(model.dnsName,
				DiskImageRecipeBuilder.buildDiskImageRecipe(this));
		// instance.minimumMemoryMb = 2048;
		addChild(instance);

		instance.addChild(PackageDependency.build("wordpress"));

		instance.addChild(ApacheBootstrap.build());

		{
			PlatformLayerFirewallEntry net = injected(PlatformLayerFirewallEntry.class);

			net.destItem = model.databaseItem;
			net.port = 3306;
			net.uniqueId = getFirewallUniqueId();

			PlatformLayerKey sourceKey = model.getKey();
			net.sourceItemKey = sourceKey;

			instance.addChild(net);
		}

		WordpressTemplateData templateData = Injection.getInstance(WordpressTemplateData.class);

		MysqlConnection mysql = instance.addChild(MysqlConnection.build(model.databaseItem));
		mysql.password = model.databasePassword;

		{
			MysqlDatabase db = injected(MysqlDatabase.class);
			db.databaseName = templateData.getDatabaseName();
			mysql.addChild(db);
		}

		{
			MysqlUser db = injected(MysqlUser.class);
			db.databaseName = templateData.getDatabaseName();
			db.databaseUser = templateData.getDatabaseUser();
			db.databasePassword = templateData.getDatabasePassword();
			mysql.addChild(db);
		}

		instance.addChild(WordpressBootstrap.build());
		instance.addChild(WordpressAdminUser.build());

		instance.addChild(WordpressApacheSite.build());

		// instance.addChild(CollectdCollector.build());

		// TODO: How do we bring up wordpress securely??
		// We don't have the tables until we run install.php
		// Maybe we could POST to the install.php form

		{
			PublicEndpoint endpoint = injected(PublicEndpoint.class);
			// endpoint.network = null;
			endpoint.publicPort = 80;
			endpoint.backendPort = 80;

			// We expect nginx to front-end us, so we don't put the dnsName
			// endpoint.dnsName = model.dnsName;

			endpoint.tagItem = model.getKey();
			endpoint.parentItem = model.getKey();

			instance.addChild(endpoint);
		}

		instance.addChild(ManagedService.build("apache2"));
	}

	private String getFirewallUniqueId() {
		// Not implemented at the moment
		throw new UnsupportedOperationException();
	}
}
