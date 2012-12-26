package org.platformlayer.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AtomHelpers {
	private static final Logger log = LoggerFactory.getLogger(AtomHelpers.class);

	public static int getCode(final JdbcConnection jdbcConnection, final String tableName, final String value)
			throws SQLException {
		String key = AtomHelpers.class.getName() + "::" + tableName + "::" + value;

		return jdbcConnection.getCacheable(key, new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				Connection connection = jdbcConnection.getConnection();
				while (true) {
					{
						String sql = "SELECT id FROM " + tableName + " WHERE key=?";

						PreparedStatement ps = connection.prepareStatement(sql);
						ResultSet rs = null;
						try {
							ps.setString(1, value);
							rs = ps.executeQuery();
							log.debug("Executing SQL: " + sql);

							while (rs.next()) {
								return rs.getInt(1);
							}
						} finally {
							JdbcUtils.safeClose(rs);
							JdbcUtils.safeClose(ps);
						}
					}

					{
						String sql = "INSERT INTO " + tableName + " (key) VALUES (?)";
						PreparedStatement ps = connection.prepareStatement(sql);
						ResultSet rs = null;
						try {
							ps.setString(1, value);
							ps.executeUpdate();
						} finally {
							JdbcUtils.safeClose(rs);
							JdbcUtils.safeClose(ps);
						}
					}
				}
			}

		});

	}

	public static String getValue(final JdbcConnection jdbcConnection, final String tableName, final int id)
			throws SQLException {
		String key = AtomHelpers.class.getName() + "::" + tableName + "::" + id;

		return jdbcConnection.getCacheable(key, new Callable<String>() {

			@Override
			public String call() throws Exception {
				Connection connection = jdbcConnection.getConnection();

				String sql = "SELECT key FROM " + tableName + " WHERE id=?";

				PreparedStatement ps = connection.prepareStatement(sql);
				ResultSet rs = null;
				try {
					ps.setInt(1, id);
					rs = ps.executeQuery();
					log.debug("Executing SQL: " + sql);
					while (rs.next()) {
						return rs.getString(1);
					}
				} finally {
					JdbcUtils.safeClose(rs);
					JdbcUtils.safeClose(ps);
				}

				return null;
			}

		});

	}

}
