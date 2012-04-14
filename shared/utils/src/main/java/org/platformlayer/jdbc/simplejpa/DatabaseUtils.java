package org.platformlayer.jdbc.simplejpa;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.postgresql.PGResultSetMetaData;

public class DatabaseUtils {

	public static String getTableName(ResultSetMetaData rsmd, int i) throws SQLException {
		String tableName = rsmd.getTableName(i);
		if (tableName.isEmpty()) {
			if (rsmd instanceof PGResultSetMetaData) {
				// TODO: Getting the table OID here would avoid a nasty metadata query
				PGResultSetMetaData pgRsmd = (PGResultSetMetaData) rsmd;
				tableName = pgRsmd.getBaseTableName(i);
			}
		}

		return tableName;
	}

}
