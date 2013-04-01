package org.platformlayer.jdbc.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

import org.platformlayer.jdbc.simplejpa.DatabaseNameMapping;
import org.platformlayer.jdbc.simplejpa.FieldMap;

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

	final boolean batchExecute;

	final List<QueryFilterDescriptor> filters;

	enum QueryType {
		Manual, AutomaticUpdate, AutomaticInsert
	}

	private QueryDescriptor(QueryType queryType, String sql, List<QueryFilterDescriptor> filters,
			List<Field> parameterMap, boolean batchExecute) {
		super();
		this.queryType = queryType;
		this.sql = sql;
		this.filters = filters;
		this.parameterMap = parameterMap;
		this.batchExecute = batchExecute;
	}

	public String getBaseSql() {
		return sql;
	}

	private static QueryDescriptor buildManual(String sql, List<QueryFilterDescriptor> filters, boolean batchExecute) {
		return new QueryDescriptor(QueryType.Manual, sql, filters, null, batchExecute);
	}

	private static QueryDescriptor buildAutomaticInsert(FieldMap map, boolean batchExecute) {
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

		return new QueryDescriptor(QueryType.AutomaticInsert, sql, null, parameterMap, batchExecute);
	}

	private static QueryDescriptor buildAutomaticUpdate(FieldMap map, boolean batchExecute) {
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
				sb.append(" AND ");
			}
			sb.append(columnName + "=?");
			parameterMap.add(map.getFieldForColumn(columnName));
			i++;
		}

		if (i == 0) {
			throw new IllegalArgumentException("No id fields found in update");
		}

		String sql = sb.toString();

		return new QueryDescriptor(QueryType.AutomaticUpdate, sql, null, parameterMap, batchExecute);
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

		List<QueryFilterDescriptor> filters = Lists.newArrayList();
		Annotation[][] parameterAnnotationsArray = m.getParameterAnnotations();
		for (int i = 0; i < parameterAnnotationsArray.length; i++) {
			Annotation[] annotations = parameterAnnotationsArray[i];
			if (annotations == null || annotations.length == 0)
				continue;

			for (Annotation annotation : annotations) {
				if (annotation.annotationType().equals(QueryFilter.class)) {
					QueryFilterDescriptor filter = new QueryFilterDescriptor(i, (QueryFilter) annotation);
					filters.add(filter);
				}
			}
		}
		FieldMap fieldMap = null;

		BatchExecute batchExecuteAnnotation = m.getAnnotation(BatchExecute.class);
		boolean batchExecute = batchExecuteAnnotation != null;
		if (queryType == QueryType.Manual) {
			queryDescriptor = QueryDescriptor.buildManual(sql, filters, batchExecute);
		} else {
			if (!filters.isEmpty()) {
				throw new IllegalArgumentException("Filters not supported on automatic query");
			}

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
				queryDescriptor = QueryDescriptor.buildAutomaticInsert(fieldMap, batchExecute);
				break;
			}
			case AutomaticUpdate: {
				queryDescriptor = QueryDescriptor.buildAutomaticUpdate(fieldMap, batchExecute);
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

	public boolean isBatchExecute() {
		return batchExecute;
	}

	public SqlWithParameters buildSql(Object[] args) throws SQLException {
		SqlWithParameters sqlBuilder = new SqlWithParameters();

		// TODO: We could pre-compute this at construction
		BitSet mappedParameters = new BitSet();
		if (this.filters != null) {
			for (QueryFilterDescriptor filter : this.filters) {
				mappedParameters.set(filter.paramIndex);
			}
		}
		if (queryType == QueryType.Manual) {
			assert parameterMap == null;

			if (args != null) {
				for (int i = 0; i < args.length; i++) {
					if (mappedParameters.get(i)) {
						continue;
					}

					Object arg = args[i];
					sqlBuilder.parameters.add(arg);
				}
			}
		} else {
			assert parameterMap != null;
			assert args.length == 1;

			try {
				for (Field field : parameterMap) {
					Object param = field.get(args[0]);
					sqlBuilder.parameters.add(param);
				}
			} catch (IllegalAccessException e) {
				throw new IllegalArgumentException("Error reading field values", e);
			}
		}

		if (filters == null || filters.isEmpty()) {
			sqlBuilder.sql = this.sql;
			return sqlBuilder;
		}

		String limit = null;
		Object limitArg = null;

		List<String> filterSql = Lists.newArrayList();
		List<Object> filterParameters = Lists.newArrayList();

		for (QueryFilterDescriptor filter : filters) {
			Object arg = args[filter.paramIndex];
			if (filter.isLimit()) {
				if (arg == null)
					continue;
				limit = "LIMIT ?";
				limitArg = arg;
			} else {
				if (arg == null)
					continue;

				filterSql.add(filter.getSql());
				filterParameters.add(arg);
			}
		}

		StringBuilder sb = new StringBuilder();

		if (!filterSql.isEmpty()) {
			String normalized = sql.toLowerCase();
			int whereIndex = normalized.indexOf(" where ");
			if (whereIndex != -1) {
				String tail = sql.substring(whereIndex + " where ".length());
				String head = sql.substring(0, whereIndex);

				sb.append(head);
				sb.append(" WHERE (( ");
				sb.append(tail);
				sb.append(" ) AND ( ");
			} else {
				sb.append(this.sql);
				sb.append(" WHERE ( ( ");
			}

			for (int i = 0; i < filterSql.size(); i++) {
				if (i != 0) {
					sb.append(" ) AND ( ");
				}
				sb.append(filterSql.get(i));
				sqlBuilder.parameters.add(filterParameters.get(i));
			}

			sb.append(" ) ) ");
		}

		// Limit must come at the end
		if (limit != null) {
			sb.append(" LIMIT ?");
			sqlBuilder.parameters.add(limitArg);
		}

		sqlBuilder.sql = sb.toString();
		return sqlBuilder;
	}
}