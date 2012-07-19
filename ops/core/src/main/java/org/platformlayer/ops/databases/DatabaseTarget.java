package org.platformlayer.ops.databases;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.platformlayer.ops.OpsException;

public abstract class DatabaseTarget {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(DatabaseTarget.class);

	public abstract SqlResults execute(String sql) throws SQLException, OpsException;

	public abstract boolean createDatabase(String databaseName) throws OpsException;
}
