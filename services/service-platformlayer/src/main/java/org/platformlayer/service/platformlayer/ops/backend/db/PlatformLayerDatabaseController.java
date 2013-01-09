package org.platformlayer.service.platformlayer.ops.backend.db;

import java.io.IOException;

import org.platformlayer.ResourceUtils;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.postgres.CreateDatabase;
import org.platformlayer.ops.postgres.CreateUser;
import org.platformlayer.ops.postgres.DatabaseConnection;
import org.platformlayer.ops.postgres.RunScript;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlatformLayerDatabaseController extends OpsTreeBase {

	private static final Logger log = LoggerFactory.getLogger(PlatformLayerDatabaseController.class);

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		PlatformLayerDatabaseTemplate template = injected(PlatformLayerDatabaseTemplate.class);

		DatabaseConnection dbConnection;
		{
			dbConnection = addChild(DatabaseConnection.build(template.getDatabaseServerKey()));

			dbConnection.databaseName = template.getDatabaseName();
			// We need to run as superuser
			// dbConnection.username = template.getAuthDatabaseUsername();
			// dbConnection.password = template.getAuthDatabasePassword();
		}

		{
			CreateDatabase db = dbConnection.addChild(CreateDatabase.class);
			db.databaseName = template.getDatabaseName();
		}

		{
			CreateUser db = dbConnection.addChild(CreateUser.class);
			db.grantDatabaseName = template.getDatabaseName();
			db.databaseUser = template.getDatabaseUsername();
			db.databasePassword = template.getDatabasePassword();
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
