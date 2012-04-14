package org.platformlayer.jdbc.simplejpa;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;

public class ResultSetMappers {
	final DatabaseNameMapping databaseNameMapping;

	final Map<String, ResultSetMapper> sqlMappers = Maps.newHashMap();
	final Map<String, Class<?>> classes = Maps.newHashMap();

	public ResultSetMappers(DatabaseNameMapping databaseMapping, Class<?>... modelClasses) {
		super();
		this.databaseNameMapping = databaseMapping;
		addClass(modelClasses);
	}

	public void addClass(Class<?>... classes) {
		for (Class<?> clazz : classes) {
			String tableName = databaseNameMapping.getTableName(clazz);
			this.classes.put(tableName, clazz);
		}
	}

	public ResultSetMapper get(String sql) {
		ResultSetMapper resultSetMapper = sqlMappers.get(sql);
		if (resultSetMapper == null) {
			resultSetMapper = new ResultSetMapper(this);
			sqlMappers.put(sql, resultSetMapper);
		}
		return resultSetMapper;
	}

	public Class<?> getClassForTableName(String tableName) {
		return classes.get(tableName);
	}

	public DatabaseNameMapping getNameMapping() {
		return this.databaseNameMapping;
	}

	final ConcurrentMap<Class<?>, Relationships> relationshipsByClass = new MapMaker()
			.makeComputingMap(new Function<Class<?>, Relationships>() {
				@Override
				public Relationships apply(Class<?> sourceClass) {
					return buildRelationships(sourceClass);
				}
			});

	public void populateInternalRelationships(JoinedQueryResult mapResult) throws SecurityException,
			NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		for (Class<?> resultClass : mapResult.types.keySet()) {
			Relationships relationships = relationshipsByClass.get(resultClass);
			relationships.doMap(mapResult);
		}
	}

	protected Relationships buildRelationships(Class<?> sourceClass) {
		Relationships relationships = new Relationships(sourceClass);

		return relationships;
	}
}
