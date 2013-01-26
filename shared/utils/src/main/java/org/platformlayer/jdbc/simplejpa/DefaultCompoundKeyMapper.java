package org.platformlayer.jdbc.simplejpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class DefaultCompoundKeyMapper extends KeyMapper {
	private final List<Integer> columnNumbers;

	public DefaultCompoundKeyMapper(List<Integer> columnNumbers) {
		this.columnNumbers = columnNumbers;
	}

	static class DefaultCompoundKey {
		final Object[] key;

		public DefaultCompoundKey(Object[] key) {
			super();
			this.key = key;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(key);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DefaultCompoundKey other = (DefaultCompoundKey) obj;
			if (!Arrays.equals(key, other.key))
				return false;
			return true;
		}
	}

	@Override
	public Object getKey(ResultSet rs) throws SQLException {
		Object[] pk = new Object[columnNumbers.size()];
		for (int i = 0; i < pk.length; i++) {
			pk[i] = rs.getObject(columnNumbers.get(i));
		}
		return new DefaultCompoundKey(pk);
	}
}
