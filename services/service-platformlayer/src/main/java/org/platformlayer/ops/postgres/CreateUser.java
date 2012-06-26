package org.platformlayer.ops.postgres;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.Secret;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.databases.DatabaseTarget;

import com.google.common.base.Objects;

public class CreateUser {
	static final Logger log = Logger.getLogger(CreateUser.class);

	public String grantDatabaseName;
	public String databaseUser;
	public Secret databasePassword;

	@Handler
	public void handler(DatabaseTarget db) throws OpsException {
		if (OpsContext.isConfigure()) {
			try {
				String createUser = String.format("CREATE USER %s WITH PASSWORD '%s'", databaseUser,
						databasePassword.plaintext());
				db.execute(createUser);
			} catch (SQLException e) {
				String sqlState = e.getSQLState();
				if (Objects.equal(sqlState, "42710")) {
					// ProcessExecution execution = e.getExecution();
					// if (execution.getExitCode() == 1 && execution.getStdErr().contains("already exists")) {
					log.info("User already exists");
				} else {
					throw new OpsException("Error creating user", e);
				}
			}
			String grant = String.format("GRANT ALL PRIVILEGES ON DATABASE %s to %s;", grantDatabaseName, databaseUser);
			try {
				db.execute(grant);
			} catch (SQLException e) {
				// String sqlState = e.getSQLState();
				// if (Objects.equal(sqlState, "12345")) {
				// log.info("User already exists");
				// } else {
				throw new OpsException("Error granting privileges", e);
				// }
			}

		}
	}
}
