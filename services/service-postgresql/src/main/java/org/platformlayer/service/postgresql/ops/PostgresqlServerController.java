package org.platformlayer.service.postgresql.ops;

import java.io.File;
import java.net.InetAddress;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.InetAddressChooser;
import org.platformlayer.core.model.Secret;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.databases.Database;
import org.platformlayer.ops.databases.DatabaseTarget;
import org.platformlayer.ops.databases.TunneledDatabaseTarget;
import org.platformlayer.ops.filesystem.TemplatedFile;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.instances.DiskImageRecipeBuilder;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.machines.InetAddressUtils;
import org.platformlayer.ops.metrics.collectd.CollectdCollector;
import org.platformlayer.ops.metrics.collectd.ManagedService;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.networks.PublicEndpoint;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.postgresql.model.PostgresqlServer;

public class PostgresqlServerController extends OpsTreeBase implements Database {
	static final Logger log = Logger.getLogger(PostgresqlServerController.class);

	@Inject
	InstanceHelpers instanceHelpers;

	@Handler
	public void handler() throws OpsException {
	}

	@Override
	protected void addChildren() throws OpsException {
		PostgresqlTemplateVariables template = injected(PostgresqlTemplateVariables.class);
		PostgresqlServer model = template.getModel();

		InstanceBuilder instance = InstanceBuilder.build(model.dnsName,
				DiskImageRecipeBuilder.buildDiskImageRecipe(this));
		// TODO: Memory _really_ needs to be configurable here!
		instance.publicPorts.add(5432);

		instance.minimumMemoryMb = 2048;
		addChild(instance);

		instance.addChild(PackageDependency.build("postgresql"));
		instance.addChild(PackageDependency.build("postgresql-client"));

		String postgresVersion = template.getPostgresVersion();

		if (postgresVersion.equals("8.4")) {
			instance.addChild(TemplatedFile.build(template, new File("/etc/postgresql/8.4/main/pg_hba.conf"),
					"8_4_pg_hba.conf"));
			instance.addChild(TemplatedFile.build(template, new File("/etc/postgresql/8.4/main/postgresql.conf"),
					"8_4_postgresql.conf"));
		} else if (postgresVersion.equals("9.1")) {
			instance.addChild(TemplatedFile.build(template, new File("/etc/postgresql/9.1/main/pg_hba.conf"),
					"9_1_pg_hba.conf"));
			instance.addChild(TemplatedFile.build(template, new File("/etc/postgresql/9.1/main/postgresql.conf"),
					"9_1_postgresql.conf"));
		} else {
			throw new OpsException("Unsupported postgres version: " + postgresVersion);
		}

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

	@Override
	public String getJdbcUrl(Object obj, String databaseName, InetAddressChooser chooser) throws OpsException {
		PostgresqlServer database = (PostgresqlServer) obj;

		Machine itemMachine = instanceHelpers.getMachine(database);

		InetAddress address = itemMachine.getBestAddress(NetworkPoint.forTargetInContext(), 5432, chooser);
		String host = address.getHostAddress();
		if (InetAddressUtils.isIpv6(address)) {
			host = "[" + host + "]";
		}
		return "jdbc:postgresql://" + host + ":5432/" + databaseName;
	}

	@Override
	public Secret getRootPassword(Object obj) {
		PostgresqlServer database = (PostgresqlServer) obj;

		return database.rootPassword;
	}

	@Override
	public String getRootUsername(Object objr) {
		return "postgres";
	}

	@Override
	public DatabaseTarget buildDatabaseTarget(Object obj, String username, Secret password, String databaseName)
			throws OpsException {
		PostgresqlServer server = (PostgresqlServer) obj;
		OpsTarget target = instanceHelpers.getTarget(server);

		// Machine machine = instanceHelpers.getMachine(pgServer);
		//
		// String address = machine.getBestAddress(NetworkPoint.forTargetInContext(), POSTGRES_PORT);
		// PostgresTarget mysql = new TunneledPostgresTarget(address, username, password);

		DatabaseTarget db = new TunneledDatabaseTarget(target, username, password, databaseName);

		return db;
	}
}
