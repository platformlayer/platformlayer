package org.platformlayer.ops.postgres;

import java.sql.SQLException;

import org.slf4j.*;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.databases.DatabaseTarget;

import com.google.common.base.Strings;

public class RunScript {
	static final Logger log = LoggerFactory.getLogger(RunScript.class);

	public String sql;

	@Handler
	public void handler(DatabaseTarget db) throws OpsException {
		if (OpsContext.isConfigure()) {
			if (Strings.isNullOrEmpty(sql)) {
				log.info("No script; skipping execution");
			} else {
				try {
					db.execute(sql);
				} catch (SQLException e) {
					throw new OpsException("Error running script", e);
				}
			}
		}
	}
}
