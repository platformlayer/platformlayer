package org.platformlayer.ops.postgres;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.Secret;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.process.ProcessExecution;
import org.platformlayer.ops.process.ProcessExecutionException;

public class PostgresUser {
	static final Logger log = Logger.getLogger(PostgresUser.class);

	public String databaseName;
	public String databaseUser;
	public Secret databasePassword;

	@Handler
	public void handler(PostgresTarget db) throws OpsException {
		if (OpsContext.isConfigure()) {
			try {
				String createUser = String.format("CREATE USER %s WITH PASSWORD '%s'", databaseUser,
						databasePassword.plaintext());
				db.execute(createUser);
			} catch (ProcessExecutionException e) {
				ProcessExecution execution = e.getExecution();
				if (execution.getExitCode() == 1 && execution.getStdErr().contains("already exists")) {
					log.info("User already exists");
				} else {
					throw new OpsException("Error creating database", e);
				}
			}
			String grant = String.format("GRANT ALL PRIVILEGES ON DATABASE %s to %s;", databaseName, databaseUser);
			db.execute(grant);
		}
	}
}
