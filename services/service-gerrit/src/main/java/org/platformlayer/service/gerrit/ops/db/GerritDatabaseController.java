package org.platformlayer.service.gerrit.ops.db;

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

public class GerritDatabaseController extends OpsTreeBase {
	static final Logger log = Logger.getLogger(GerritDatabaseController.class);

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		GerritDatabaseTemplate template = injected(GerritDatabaseTemplate.class);

		DatabaseConnection dbConnection;
		{
			dbConnection = addChild(DatabaseConnection.build(template.getDatabaseServerKey()));

			dbConnection.databaseName = template.getDatabaseName();
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
