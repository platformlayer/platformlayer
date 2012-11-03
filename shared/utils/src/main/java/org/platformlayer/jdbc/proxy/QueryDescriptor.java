package org.platformlayer.jdbc.proxy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.platformlayer.jdbc.simplejpa.DatabaseNameMapping;
import org.platformlayer.jdbc.simplejpa.FieldMap;
import org.platformlayer.shared.EnumWithKey;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * QueryDescriptor builds the SQL statement (but doesn't process the results)
 * 
 */
class QueryDescriptor {
	static final Map<String, QueryDescriptor> CACHE = Maps.newHashMap();

	final String sql;
	final QueryType queryType;

	final List<Field> parameterMap;

	enum QueryType {
		Manual, AutomaticUpdate, AutomaticInsert
	}

	private QueryDescriptor(QueryType queryType, String sql, List<Field> parameterMap) {
		super();
		this.queryType = queryType;
		this.sql = sql;
		this.parameterMap = parameterMap;
	}

	public void setParameters(PreparedStatement ps, Object[] args) throws SQLException {
		if (queryType == QueryType.Manual) {
			assert parameterMap == null;

			if (args != null) {
				for (int i = 0; i < args.length; i++) {
					ps.setObject(i + 1, args[i]);
				}
			}
		} else {
			assert parameterMap != null;
			assert args.length == 1;

			try {
				int i = 0;
				for (Field field : parameterMap) {
					Object param = field.get(args[0]);

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

					i++;
				}
			} catch (IllegalAccessException e) {
				throw new IllegalArgumentException("Error reading field values", e);
			}
		}
	}

	public String getSql() {
		return sql;
	}

	private static QueryDescriptor buildManual(String sql) {
		return new QueryDescriptor(QueryType.Manual, sql, null);
	}

	private static QueryDescriptor buildAutomaticInsert(FieldMap map) {
		List<String> columnNames = map.getColumnNames();

		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ");
		sb.append(map.getTableName());
		sb.append(" (");
		sb.append(Joiner.on(",").join(columnNames));
		sb.append(") VALUES (");
		for (int i = 0; i < columnNames.size(); i++) {
			if (i != 0) {
				sb.append(",");
			}
			sb.append("?");
		}
		sb.append(")");

		List<Field> parameterMap = Lists.newArrayList();
		for (String columnName : map.getColumnNames()) {
			parameterMap.add(map.getFieldForColumn(columnName));
		}

		String sql = sb.toString();

		return new QueryDescriptor(QueryType.AutomaticInsert, sql, parameterMap);
	}

	private static QueryDescriptor buildAutomaticUpdate(FieldMap map) {
		List<String> columnNames = map.getColumnNames();
		List<Field> parameterMap = Lists.newArrayList();

		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ");
		sb.append(map.getTableName());
		int i = 0;
		for (String columnName : columnNames) {
			if (map.isId(columnName)) {
				continue;
			}

			if (i == 0) {
				sb.append(" SET ");
			} else {
				sb.append(", ");
			}
			sb.append(columnName + "=?");
			parameterMap.add(map.getFieldForColumn(columnName));
			i++;
		}

		i = 0;
		for (String columnName : columnNames) {
			if (!map.isId(columnName)) {
				continue;
			}

			if (i == 0) {
				sb.append(" WHERE ");
			} else {
				sb.append(", ");
			}
			sb.append(columnName + "=?");
			parameterMap.add(map.getFieldForColumn(columnName));
			i++;
		}

		if (i == 0) {
			throw new IllegalArgumentException("No id fields found in update");
		}

		String sql = sb.toString();

		return new QueryDescriptor(QueryType.AutomaticUpdate, sql, parameterMap);
	}

	public static QueryDescriptor getQueryDescriptor(Method m) {
		Query query = m.getAnnotation(Query.class);
		if (query == null) {
			throw new UnsupportedOperationException();
		}

		String cacheKey = m.getDeclaringClass().getName() + "::" + m.getName();

		QueryDescriptor queryDescriptor;

		synchronized (QueryDescriptor.CACHE) {
			queryDescriptor = QueryDescriptor.CACHE.get(cacheKey);
		}

		String sql = query.value();

		QueryType queryType = QueryType.Manual;

		// TODO: We could just infer this from the method name...
		if (sql.equals(Query.AUTOMATIC_INSERT)) {
			queryType = QueryType.AutomaticInsert;
		} else if (sql.equals(Query.AUTOMATIC_UPDATE)) {
			queryType = QueryType.AutomaticUpdate;
		}

		FieldMap fieldMap = null;

		if (queryType == QueryType.Manual) {
			queryDescriptor = QueryDescriptor.buildManual(sql);
		} else {
			Class<?>[] parameterTypes = m.getParameterTypes();
			if (parameterTypes.length != 1) {
				throw new IllegalArgumentException("Expected exactly one argument for automatic query");
			}

			Class<?> c = parameterTypes[0];

			// TODO: We could check the connection!
			DatabaseNameMapping nameMapping = DatabaseNameMapping.POSTGRESQL;
			fieldMap = FieldMap.build(nameMapping, c);

			switch (queryType) {
			case AutomaticInsert: {
				queryDescriptor = QueryDescriptor.buildAutomaticInsert(fieldMap);
				break;
			}
			case AutomaticUpdate: {
				queryDescriptor = QueryDescriptor.buildAutomaticUpdate(fieldMap);
				break;
			}
			default:
				throw new IllegalStateException();
			}
		}

		synchronized (QueryDescriptor.CACHE) {
			QueryDescriptor.CACHE.put(cacheKey, queryDescriptor);
		}

		return queryDescriptor;
	}
}