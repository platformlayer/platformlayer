package org.platformlayer.jdbc;

import java.sql.SQLException;

import org.platformlayer.model.StringWrapper;

import com.fathomdb.jdbc.JdbcConnection;

public class DbAtom<T> {
	T value;

	int code;

	public int getCode(JdbcConnection connection) throws SQLException {
		if (code == 0) {
			Class<?> valueClass = value.getClass();
			String tableName = getTableName(valueClass);
			String key = ((StringWrapper) value).getKey();
			code = AtomHelpers.getCode(connection, tableName, key);
		}
		return code;
	}

	private static String getTableName(Class<?> valueClass) {
		String tableName = valueClass.getSimpleName();
		tableName = tableName.toLowerCase();
		if (tableName.equals("servicetype")) {
			tableName = "services";
		}
		if (tableName.equals("projectid")) {
			tableName = "project_codes";
		}
		if (tableName.equals("itemtype")) {
			tableName = "item_types";
		}
		if (tableName.equals("servicemetadatakey")) {
			tableName = "metadata_keys";
		}
		return tableName;
	}

	protected static String mapCodeToKey(JdbcConnection connection, Class<?> clazz, int model) throws SQLException {
		String tableName = getTableName(clazz);
		return AtomHelpers.getValue(connection, tableName, model);
	}

	protected static <T> int mapKeyToCode(JdbcConnection connection, T t) throws SQLException {
		DbAtom<T> atom = DbAtom.build();
		atom.setValue(t);
		return atom.getCode(connection);
	}

	public static <T> DbAtom<T> build() {
		return new DbAtom<T>();
	}

	public void setValue(T value) {
		this.value = value;
		this.code = 0;
	}

}
