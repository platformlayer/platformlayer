package org.platformlayer.service.database.ops;

import java.util.Map;

import javax.inject.Inject;

import org.platformlayer.InetAddressChooser;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.databases.DatabaseServer;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.helpers.ProviderHelper;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.postgres.CreateDatabase;
import org.platformlayer.ops.postgres.CreateUser;
import org.platformlayer.ops.postgres.DatabaseConnection;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.ops.uses.LinkConsumer;
import org.platformlayer.ops.uses.LinkTarget;
import org.platformlayer.service.database.model.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseController extends OpsTreeBase implements LinkTarget {
	private static final Logger log = LoggerFactory.getLogger(DatabaseController.class);

	@Bound
	Database model;

	@Inject
	InstanceHelpers instanceHelpers;

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	ProviderHelper providers;

	@Handler
	public void handler() throws OpsException {
	}

	@Override
	protected void addChildren() throws OpsException {
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
	}

	@Override
	public Map<String, String> buildLinkTargetConfiguration(LinkConsumer consumer) throws OpsException {
		ItemBase serverItem = platformLayer.getItem(model.server);
		DatabaseServer databaseServer = providers.toInterface(serverItem, DatabaseServer.class);

		InetAddressChooser inetAddressChooser = consumer.getInetAddressChooser();

		return databaseServer.buildTargetConfiguration(model.username, model.password, model.databaseName,
				inetAddressChooser);
	}

	@Override
	public PlatformLayerKey getCaForClientKey() {
		return null;
	}
}
