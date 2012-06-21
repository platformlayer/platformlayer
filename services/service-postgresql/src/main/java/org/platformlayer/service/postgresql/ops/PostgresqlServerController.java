package org.platformlayer.service.postgresql.ops;

import java.io.File;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.filesystem.TemplatedFile;
import org.platformlayer.ops.instances.DiskImageRecipeBuilder;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.metrics.collectd.CollectdCollector;
import org.platformlayer.ops.metrics.collectd.ManagedService;
import org.platformlayer.ops.networks.PublicEndpoint;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.ops.templates.TemplateDataSource;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.postgresql.model.PostgresqlServer;

public class PostgresqlServerController extends OpsTreeBase {
	static final Logger log = Logger.getLogger(PostgresqlServerController.class);

	@Handler
	public void handler() throws OpsException {
	}

	@Override
	protected void addChildren() throws OpsException {
		PostgresqlServer model = OpsContext.get().getInstance(PostgresqlServer.class);

		InstanceBuilder instance = InstanceBuilder.build(model.dnsName,
				DiskImageRecipeBuilder.buildDiskImageRecipe(this));
		// TODO: Memory _really_ needs to be configurable here!
		instance.publicPorts.add(5432);

		instance.minimumMemoryMb = 2048;
		addChild(instance);

		instance.addChild(PackageDependency.build("postgresql"));
		instance.addChild(PackageDependency.build("postgresql-client"));

		TemplateDataSource templateVars = new PostgresqlTemplateVariables();
		instance.addChild(TemplatedFile.build(templateVars, new File("/etc/postgresql/8.4/main/pg_hba.conf")));
		instance.addChild(TemplatedFile.build(templateVars, new File("/etc/postgresql/8.4/main/postgresql.conf")));

		instance.addChild(PostgresqlServerBootstrap.build());

		instance.addChild(CollectdCollector.build());

		{
			PublicEndpoint endpoint = injected(PublicEndpoint.class);
			// endpoint.network = null;
			endpoint.publicPort = 5432;
			endpoint.backendPort = 5432;
			endpoint.dnsName = model.dnsName;

			endpoint.tagItem = model.getKey();
			endpoint.parentItem = model.getKey();

			instance.addChild(endpoint);
		}

		instance.addChild(ManagedService.build("postgresql"));

		instance.addChild(injected(PostgresqlServerBackup.class));
	}
}
