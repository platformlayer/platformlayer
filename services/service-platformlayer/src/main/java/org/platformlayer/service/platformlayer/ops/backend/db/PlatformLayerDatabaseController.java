package org.platformlayer.service.platformlayer.ops.backend.db;

import java.io.IOException;

import org.platformlayer.ResourceUtils;
import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.postgres.CreateDatabase;
import org.platformlayer.ops.postgres.CreateUser;
import org.platformlayer.ops.postgres.DatabaseConnection;
import org.platformlayer.ops.postgres.RunScript;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.platformlayer.model.PlatformLayerDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlatformLayerDatabaseController extends OpsTreeBase {

	private static final Logger log = LoggerFactory.getLogger(PlatformLayerDatabaseController.class);

	@Bound
	PlatformLayerDatabase model;

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		DatabaseConnection dbConnection;
		{
			dbConnection = addChild(DatabaseConnection.build(model.server));

			dbConnection.databaseName = model.databaseName;
			// We need to run as superuser
			// dbConnection.username = template.getAuthDatabaseUsername();
			// dbConnection.password = template.getAuthDatabasePassword();
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

		{
			RunScript script = dbConnection.addChild(RunScript.class);
			try {
				script.sql = ResourceUtils.get(getClass(), "schema.sql");
			} catch (IOException e) {
				throw new OpsException("Error loading SQL script resource", e);
			}
		}
	}
}
