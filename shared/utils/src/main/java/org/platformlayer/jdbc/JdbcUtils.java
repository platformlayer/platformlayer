package org.platformlayer.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.platformlayer.IoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcUtils {
	static final Logger log = LoggerFactory.getLogger(IoUtils.class);

	public static void safeClose(Connection closeable) {
		if (closeable == null) {
			return;
		}
		try {
			closeable.close();
		} catch (SQLException e) {
			log.error("Ignoring unexpected error closing connection", e);
		}
	}

	public static void safeClose(Statement closeable) {
		if (closeable == null) {
			return;
		}
		try {
			closeable.close();
		} catch (SQLException e) {
			log.error("Ignoring unexpected error closing statement", e);
		}
	}

	public static void safeClose(ResultSet rs) {
		if (rs == null) {
			return;
		}
		try {
			rs.close();
		} catch (SQLException e) {
			log.error("Ignoring unexpected error closing resultset", e);
		}
	}
}
