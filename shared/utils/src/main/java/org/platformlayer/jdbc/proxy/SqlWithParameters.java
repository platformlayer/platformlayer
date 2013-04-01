package org.platformlayer.jdbc.proxy;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.platformlayer.shared.EnumWithKey;

import com.google.common.collect.Lists;

public class SqlWithParameters {
	String sql;
	final List<Object> parameters = Lists.newArrayList();

	public void setParameters(PreparedStatement ps) throws SQLException {
		for (int i = 0; i < parameters.size(); i++) {
			Object arg = parameters.get(i);
			setParameter(ps, i, arg);
		}
	}

	private void setParameter(PreparedStatement ps, int i, Object param) throws SQLException {
		if (param instanceof EnumWithKey) {
			String key = ((EnumWithKey) param).getKey();
			ps.setString(i + 1, key);
		} else if (param instanceof java.util.Date) {
			java.util.Date date = (java.util.Date) param;
			Timestamp timestamp = new Timestamp(date.getTime());

			ps.setTimestamp(i + 1, timestamp);
			// if (!(param instanceof java.sql.Date)) {
			// ps.setDate(i + 1, (java.util.Date) param);
			// }
		} else {
			ps.setObject(i + 1, param);
		}
	}

	public String getSql() {
		return sql;
	}

	@Override
	public String toString() {
		return "SqlWithParameters [sql=" + sql + "]";
	}

}
