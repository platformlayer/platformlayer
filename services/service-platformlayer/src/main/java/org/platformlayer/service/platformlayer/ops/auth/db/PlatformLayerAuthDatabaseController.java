package org.platformlayer.service.platformlayer.ops.auth.db;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.platformlayer.ResourceUtils;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.postgres.CreateDatabase;
import org.platformlayer.ops.postgres.CreateUser;
import org.platformlayer.ops.postgres.DatabaseConnection;
import org.platformlayer.ops.postgres.RunScript;
import org.platformlayer.ops.tree.OpsTreeBase;

public class PlatformLayerAuthDatabaseController extends OpsTreeBase {
	static final Logger log = Logger.getLogger(PlatformLayerAuthDatabaseController.class);

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		PlatformLayerAuthDatabaseTemplate template = injected(PlatformLayerAuthDatabaseTemplate.class);

		DatabaseConnection dbConnection;
		{
			dbConnection = addChild(DatabaseConnection.build(template.getAuthDatabaseServerKey()));

			dbConnection.databaseName = template.getAuthDatabaseName();
			// We need to run as superuser
			// dbConnection.username = template.getAuthDatabaseUsername();
			// dbConnection.password = template.getAuthDatabasePassword();
		}

		{
			CreateDatabase db = dbConnection.addChild(CreateDatabase.class);
			db.databaseName = template.getAuthDatabaseName();
		}

		{
			CreateUser db = dbConnection.addChild(CreateUser.class);
			db.grantDatabaseName = template.getAuthDatabaseName();
			db.databaseUser = template.getAuthDatabaseUsername();
			db.databasePassword = template.getAuthDatabasePassword();
		}

		{
			RunScript script = dbConnection.addChild(RunScript.class);
			try {
				script.sql = ResourceUtils.get(getClass(), "auth_schema.sql");
			} catch (IOException e) {
				throw new OpsException("Error loading SQL script resource", e);
			}

		}
	}
}
