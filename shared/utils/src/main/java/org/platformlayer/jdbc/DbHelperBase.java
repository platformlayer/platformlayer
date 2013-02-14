package org.platformlayer.jdbc;

import java.io.Closeable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import com.google.common.collect.Maps;

public class DbHelperBase implements Closeable {
	protected final JdbcConnection jdbcConnection;
	final Map<String, PreparedStatement> preparedStatements = Maps.newHashMap();

	final Map<Class<?>, DbAtom<?>> atoms = Maps.newHashMap();

	protected <T> void setAtom(T o) {
		if (o == null) {
			throw new IllegalArgumentException();
		}
		Class<T> clazz = (Class<T>) o.getClass();
		DbAtom<T> atom = (DbAtom<T>) atoms.get(clazz);
		if (atom == null) {
			atom = DbAtom.build();
			atoms.put(clazz, atom);
		}
		atom.setValue(o);
	}

	protected DbHelperBase(JdbcConnection jdbcConnection) {
		this.jdbcConnection = jdbcConnection;
	}

	protected PreparedStatement prepareStatement(String sql) throws SQLException {
		PreparedStatement ps = preparedStatements.get(sql);
		if (ps == null) {
			ps = jdbcConnection.prepareStatement(sql);
			preparedStatements.put(sql, ps);
		}
		return ps;
	}

	@Override
	public void close() {
		for (PreparedStatement ps : preparedStatements.values()) {
			JdbcUtils.safeClose(ps);
		}
	}

	protected int getAtomValue(DbAtom<?> atom) throws SQLException {
		return atom.getCode(jdbcConnection);
	}

	protected int getAtomValue(Class<?> clazz) throws SQLException {
		DbAtom<?> atom = atoms.get(clazz);
		if (atom == null) {
			throw new IllegalArgumentException();
		}
		return getAtomValue(atom);
	}

	protected String mapCodeToKey(Class<?> clazz, int model) throws SQLException {
		// TODO: Check whether our in-scope atom has the value already??
		// DbAtom<?> atom = atoms.get(clazz);
		// if (atom != null) {
		// }
		return DbAtom.mapCodeToKey(jdbcConnection, clazz, model);
	}

	public <T> int mapToValue(T t) throws SQLException {
		return DbAtom.mapKeyToCode(jdbcConnection, t);
	}

	protected void setAtom(PreparedStatement ps, int parameterIndex, Class<?> clazz) throws SQLException {
		ps.setInt(parameterIndex, getAtomValue(clazz));
	}

	public JdbcConnection getJdbcConnection() {
		return jdbcConnection;
	}

}
