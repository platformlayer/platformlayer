package org.platformlayer.ops.postgres;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.databases.DatabaseTarget;

import com.google.common.base.Objects;

public class CreateDatabase {
	static final Logger log = Logger.getLogger(CreateDatabase.class);

	public String databaseName;

	@Handler
	public void handler(DatabaseTarget db) throws OpsException {
		if (OpsContext.isConfigure()) {
			try {
				db.execute(String.format("CREATE DATABASE %s", databaseName));
				// } catch (ProcessExecutionException e) {
				// ProcessExecution execution = e.getExecution();
				// if (execution.getExitCode() == 1 && execution.getStdErr().contains("already exists")) {
				// log.info("Database already exists");
				// return;
				// }
				// throw new OpsException("Error creating database", e);
			} catch (SQLException e) {
				String sqlState = e.getSQLState();
				// if (execution.getExitCode() == 1 && execution.getStdErr().contains("already exists")) {
				// log.info("Database already exists");
				// return;
				// }

				if (Objects.equal(sqlState, "XX000")) {
					log.info("Database already exists");
					return;
				}
				throw new OpsException("Error creating database", e);

			}
		}
	}

}
