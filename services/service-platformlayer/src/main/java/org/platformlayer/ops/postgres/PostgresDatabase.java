package org.platformlayer.ops.postgres;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.process.ProcessExecution;
import org.platformlayer.ops.process.ProcessExecutionException;

public class PostgresDatabase {
	static final Logger log = Logger.getLogger(PostgresDatabase.class);

	public String databaseName;

	@Handler
	public void handler(PostgresTarget db) throws OpsException {
		if (OpsContext.isConfigure()) {
			try {
				db.execute(String.format("CREATE DATABASE %s", databaseName));
			} catch (ProcessExecutionException e) {
				ProcessExecution execution = e.getExecution();
				if (execution.getExitCode() == 1 && execution.getStdErr().contains("already exists")) {
					log.info("Database already exists");
					return;
				}
				throw new OpsException("Error creating database", e);
			}
		}
	}

}
