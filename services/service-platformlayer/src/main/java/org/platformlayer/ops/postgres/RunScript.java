package org.platformlayer.ops.postgres;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.databases.DatabaseTarget;

public class RunScript {
	static final Logger log = Logger.getLogger(RunScript.class);

	public String sql;

	@Handler
	public void handler(DatabaseTarget db) throws OpsException {
		if (OpsContext.isConfigure()) {
			try {
				db.execute(sql);
			} catch (SQLException e) {
				throw new OpsException("Error running script", e);
			}

		}
	}
}
