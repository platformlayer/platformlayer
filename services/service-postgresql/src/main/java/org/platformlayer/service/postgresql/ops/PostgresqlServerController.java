package org.platformlayer.service.postgresql.ops;

import java.io.File;
import java.net.InetAddress;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.inject.Inject;

import org.platformlayer.InetAddressChooser;
import org.platformlayer.core.model.Secret;
import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.databases.DatabaseServer;
import org.platformlayer.ops.databases.DatabaseTarget;
import org.platformlayer.ops.databases.TunneledDatabaseTarget;
import org.platformlayer.ops.filesystem.TemplatedFile;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.machines.InetAddressUtils;
import org.platformlayer.ops.metrics.MetricsInstance;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.networks.PublicEndpoint;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.ops.service.ManagedService;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.postgresql.model.PostgresqlServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.crypto.OpenSshUtils;
import com.google.common.collect.Maps;

public class PostgresqlServerController extends OpsTreeBase implements DatabaseServer {

	private static final Logger log = LoggerFactory.getLogger(PostgresqlServerController.class);

	@Inject
	InstanceHelpers instanceHelpers;

	@Bound
	PostgresqlServer model;

	@Handler
	public void handler() throws OpsException {
	}

	@Override
	protected void addChildren() throws OpsException {
		PostgresqlTemplateVariables template = injected(PostgresqlTemplateVariables.class);

		InstanceBuilder instance = InstanceBuilder.build(model.dnsName, this, model.getTags());
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

		instance.addChild(MetricsInstance.class);

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
	public String getJdbcUrl(String databaseName, InetAddressChooser chooser) throws OpsException {
		Machine itemMachine = instanceHelpers.getMachine(model);

		InetAddress address = itemMachine.getNetworkPoint().getBestAddress(NetworkPoint.forTargetInContext(), chooser);
		String host = address.getHostAddress();
		if (InetAddressUtils.isIpv6(address)) {
			host = "[" + host + "]";
		}
		return "jdbc:postgresql://" + host + ":5432/" + databaseName;
	}

	@Override
	public Secret getRootPassword() {
		return model.rootPassword;
	}

	@Override
	public String getRootUsername() {
		return "postgres";
	}

	@Override
	public DatabaseTarget buildDatabaseTarget(String username, Secret password, String databaseName)
			throws OpsException {
		OpsTarget target = instanceHelpers.getTarget(model);

		// Machine machine = instanceHelpers.getMachine(pgServer);
		//
		// String address = machine.getBestAddress(NetworkPoint.forTargetInContext(), POSTGRES_PORT);
		// PostgresTarget mysql = new TunneledPostgresTarget(address, username, password);

		DatabaseTarget db = new TunneledDatabaseTarget(target, username, password, databaseName);

		return db;
	}

	@Override
	public X509Certificate[] getCertificateChain() {
		// Not yet supported
		return null;
	}

	@Override
	public Map<String, String> buildTargetConfiguration(String username, Secret password, String databaseName,
			InetAddressChooser inetAddressChooser) throws OpsException {
		Map<String, String> config = Maps.newHashMap();
		config.put("jdbc.driverClassName", "org.postgresql.Driver");

		String jdbcUrl = getJdbcUrl(databaseName, inetAddressChooser);

		config.put("jdbc.url", jdbcUrl);
		config.put("jdbc.username", username);
		config.put("jdbc.password", password.plaintext());

		X509Certificate[] sslCertificate = getCertificateChain();

		boolean useSsl = (sslCertificate != null);
		config.put("jdbc.ssl", String.valueOf(useSsl));

		if (useSsl) {
			String sigString = OpenSshUtils.getSignatureString(sslCertificate[0].getPublicKey());
			config.put("jdbc.ssl.keys", sigString);
		}

		return config;

	}
}
