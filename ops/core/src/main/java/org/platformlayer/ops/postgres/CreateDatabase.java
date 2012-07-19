package org.platformlayer.ops.postgres;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.databases.DatabaseTarget;

public class CreateDatabase {
	static final Logger log = Logger.getLogger(CreateDatabase.class);

	public String databaseName;

	@Handler
	public void handler(DatabaseTarget db) throws OpsException {
		if (OpsContext.isConfigure()) {
			if (!db.createDatabase(databaseName)) {
				log.info("Database not created - already exists");
			}
		}
	}

}
