package org.platformlayer.ops.databases;

import java.sql.SQLException;

import org.slf4j.*;
import org.platformlayer.ops.OpsException;

public abstract class DatabaseTarget {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(DatabaseTarget.class);

	public abstract SqlResults execute(String sql) throws SQLException, OpsException;

	public abstract boolean createDatabase(String databaseName) throws OpsException;
}
