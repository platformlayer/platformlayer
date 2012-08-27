package org.platformlayer.jdbc.simplejpa;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.postgresql.PGResultSetMetaData;
import org.postgresql.core.Field;
import org.postgresql.jdbc2.AbstractJdbc2ResultSetMetaData;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public class ConnectionMetadata {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(ConnectionMetadata.class);

	final Map<Integer, String> tableInfoCache = Maps.newHashMap();

	public String getTableName(ResultSetMetaData rsmd, int i) throws SQLException {
		String tableName = rsmd.getTableName(i);
		if (Strings.isNullOrEmpty(tableName)) {
			if (rsmd instanceof PGResultSetMetaData) {
				Integer tableOid = null;

				if (rsmd instanceof AbstractJdbc2ResultSetMetaData) {
					Field[] fields = getFields((AbstractJdbc2ResultSetMetaData) rsmd);
					Field field = fields[i - 1];
					tableOid = field.getTableOid();
				}

				if (tableOid != null) {
					synchronized (tableInfoCache) {
						tableName = tableInfoCache.get(tableOid);
						if (tableName != null) {
							return tableName;
						}
					}
				}

				// This is an expensive query, so we cache the Oid -> table name mappings
				PGResultSetMetaData pgRsmd = (PGResultSetMetaData) rsmd;
				tableName = pgRsmd.getBaseTableName(i);

				// TODO: We could scan the fields for other tables; we could also just pre-populate the whole table with
				// a single query??
				if (tableOid != null) {
					synchronized (tableInfoCache) {
						tableInfoCache.put(tableOid, tableName);
					}
				}
			}
		}

		return tableName;
	}

	static final java.lang.reflect.Field AbstractJdbc2ResultSetMetaDataFields;

	static {
		try {
			AbstractJdbc2ResultSetMetaDataFields = AbstractJdbc2ResultSetMetaData.class.getDeclaredField("fields");
		} catch (SecurityException e) {
			throw new IllegalArgumentException("Error getting Fields from PG metdata", e);
		} catch (NoSuchFieldException e) {
			throw new IllegalArgumentException("Error getting Fields from PG metdata", e);
		}
		AbstractJdbc2ResultSetMetaDataFields.setAccessible(true);
	}

	private static Field[] getFields(AbstractJdbc2ResultSetMetaData rsmd) {
		try {
			return (Field[]) AbstractJdbc2ResultSetMetaDataFields.get(rsmd);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Error getting Fields from PG metdata", e);
		}
	}
}
