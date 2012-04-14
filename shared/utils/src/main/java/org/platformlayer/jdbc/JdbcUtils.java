package org.platformlayer.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.platformlayer.IoUtils;

public class JdbcUtils {
	static final Logger log = Logger.getLogger(IoUtils.class);

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
