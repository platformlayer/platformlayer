package org.platformlayer.service.database.ops;

import java.net.InetAddress;
import java.util.Map;

import javax.inject.Inject;

import org.platformlayer.InetAddressChooser;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.machines.InetAddressUtils;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.postgres.CreateDatabase;
import org.platformlayer.ops.postgres.CreateUser;
import org.platformlayer.ops.postgres.DatabaseConnection;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.ops.uses.LinkTarget;
import org.platformlayer.service.database.model.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class DatabaseController extends OpsTreeBase implements LinkTarget {
	private static final Logger log = LoggerFactory.getLogger(DatabaseController.class);

	@Bound
	Database model;

	@Inject
	InstanceHelpers instanceHelpers;

	@Inject
	PlatformLayerHelpers platformLayer;

	@Handler
	public void handler() throws OpsException {
	}

	@Override
	protected void addChildren() throws OpsException {
		// GerritDatabaseTemplate template = injected(GerritDatabaseTemplate.class);

		DatabaseConnection dbConnection;
		{
			dbConnection = addChild(DatabaseConnection.build(model.server));

			dbConnection.databaseName = model.databaseName;
		}

		{
			CreateDatabase db = dbConnection.addChild(CreateDatabase.class);
			db.databaseName = model.databaseName;
		}

		{
			CreateUser db = dbConnection.addChild(CreateUser.class);
			db.grantDatabaseName = model.databaseName;
			db.databaseUser = model.username;
			db.databasePassword = model.password;
		}

		// {
		// RunScript script = dbConnection.addChild(RunScript.class);
		// try {
		// script.sql = ResourceUtils.get(getClass(), "schema.sql");
		// } catch (IOException e) {
		// throw new OpsException("Error loading SQL script resource", e);
		// }
		// }
	}

	public String getJdbcUrl(String databaseName, InetAddressChooser chooser) throws OpsException {
		ItemBase server = platformLayer.getItem(model.server);

		Machine itemMachine = instanceHelpers.getMachine(server);

		InetAddress address = itemMachine.getBestAddress(NetworkPoint.forTargetInContext(), 5432, chooser);
		String host = address.getHostAddress();
		if (InetAddressUtils.isIpv6(address)) {
			host = "[" + host + "]";
		}
		return "jdbc:postgresql://" + host + ":5432/" + databaseName;
	}

	@Override
	public Map<String, String> buildConsumerConfiguration(InetAddressChooser inetAddressChooser) throws OpsException {
		Map<String, String> config = Maps.newHashMap();
		config.put("jdbc.driverClassName", "org.postgresql.Driver");

		config.put("jdbc.url", getJdbcUrl(model.databaseName, inetAddressChooser));
		config.put("jdbc.username", model.username);
		config.put("jdbc.password", model.password.plaintext());

		return config;
	}
}
