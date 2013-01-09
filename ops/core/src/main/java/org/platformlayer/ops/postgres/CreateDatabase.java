package org.platformlayer.ops.postgres;

import org.slf4j.*;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.databases.DatabaseTarget;

public class CreateDatabase {
	static final Logger log = LoggerFactory.getLogger(CreateDatabase.class);

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
