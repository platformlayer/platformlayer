package org.platformlayer.jdbc.simplejpa;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.platformlayer.jdbc.simplejpa.JoinedQueryResult.ObjectList;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ResultSetMapper {
	static class ResultSetMapping {
		final ResultSetSingleClassMapper[] classMapperSets;

		public ResultSetMapping(ResultSetSingleClassMapper[] classMapperSets) {
			this.classMapperSets = classMapperSets;
		}

	}

	static class Mapping {
		final List<ResultSetMapping> mappings = Lists.newArrayList();
	}

	Mapping mapping;
	private final ResultSetMappers resultSetMappers;

	// void discoverMappings() {
	// this.targetClassSets = targetClassSets;
	// this.classMapperSets = new ResultSetSingleClassMapper[targetClassSets.length][];
	// for (int i = 0; i < targetClassSets.length; i++) {
	// Class<?>[] targetClasses = targetClassSets[i];
	// classMapperSets[i] = new ResultSetSingleClassMapper[targetClasses.length];
	// for (int j = 0; j < targetClasses.length; j++) {
	// classMapperSets[i][j] = new ResultSetSingleClassMapper(targetClasses[j]);
	// }
	// }
	// }

	public ResultSetMapper(ResultSetMappers resultSetMappers) {
		this.resultSetMappers = resultSetMappers;
	}

	public JoinedQueryResult doMap(ConnectionMetadata connectionMetadata, PreparedStatement preparedStatement)
			throws SQLException {
		JoinedQueryResult result = new JoinedQueryResult();

		// We might generate the mapping in parallel on multiple threads during initialization
		Mapping mapping = this.mapping;
		boolean buildingMapping = (mapping == null);
		if (buildingMapping) {
			mapping = new Mapping();
		}

		int resultSetIndex = 0;
		while (true) {
			if (resultSetIndex != 0) {
				if ((preparedStatement.getMoreResults() == false) && (preparedStatement.getUpdateCount() == -1)) {
					break;
				}
			}
			ResultSet rs = preparedStatement.getResultSet();

			ResultSetMapping resultSetMapping;
			if (buildingMapping) {
				resultSetMapping = buildResultSetMapping(connectionMetadata, rs);
				mapping.mappings.add(resultSetMapping);
			} else {
				resultSetMapping = mapping.mappings.get(resultSetIndex);
			}
			ResultSetSingleClassMapper[] classMappers = resultSetMapping.classMapperSets;
			ObjectList<?>[] objectLists = new ObjectList[classMappers.length];

			for (int j = 0; j < classMappers.length; j++) {
				ResultSetSingleClassMapper classMapper = classMappers[j];

				ObjectList objectList = objectLists[j];
				if (objectList == null) {
					Class<?> targetClass = classMapper.getTargetClass();
					objectList = result.types.get(targetClass);
					if (objectList == null) {
						objectList = new ObjectList();
						result.types.put(targetClass, objectList);
					}
					objectLists[j] = objectList;
				}
			}

			if (classMappers.length == 1) {
				// This special case avoid the need for an @Id
				// (it's also a bit faster!)

				ResultSetSingleClassMapper classMapper = classMappers[0];
				ObjectList objectList = objectLists[0];

				while (rs.next()) {
					Object newObject = classMapper.newInstance();
					classMapper.mapRowToObject(connectionMetadata, rs, newObject);

					// We use the object as the synthetic key
					Object key = newObject;
					objectList.put(key, newObject);
				}
			} else {
				while (rs.next()) {
					for (int j = 0; j < classMappers.length; j++) {
						ResultSetSingleClassMapper classMapper = classMappers[j];
						Object key = classMapper.getKey(connectionMetadata, rs);
						if (key == null) {
							continue;
						}

						ObjectList objectList = objectLists[j];
						if (objectList.containsKey(key)) {
							continue;
						}

						Object newObject = classMapper.newInstance();
						classMapper.mapRowToObject(connectionMetadata, rs, newObject);
						objectList.put(key, newObject);
					}
				}
			}

			resultSetIndex++;

		}

		if (buildingMapping) {
			synchronized (this) {
				if (this.mapping == null) {
					this.mapping = mapping;
				}
				this.mapping = mapping;
			}
		}

		try {
			resultSetMappers.populateInternalRelationships(result);
		} catch (SecurityException e) {
			throw new IllegalStateException("Error building internal relationships", e);
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException("Error building internal relationships", e);
		} catch (NoSuchFieldException e) {
			throw new IllegalStateException("Error building internal relationships", e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Error building internal relationships", e);
		}

		return result;
	}

	private ResultSetMapping buildResultSetMapping(ConnectionMetadata connectionMetadata, ResultSet rs)
			throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		Map<String, ResultSetSingleClassMapper> tableMaps = Maps.newHashMap();

		for (int i = 0; i < rsmd.getColumnCount(); i++) {
			String tableName = connectionMetadata.getTableName(rsmd, i + 1);
			if (tableMaps.containsKey(tableName)) {
				continue;
			}

			Class<?> clazz = resultSetMappers.getClassForTableName(tableName);
			if (clazz == null) {
				throw new IllegalArgumentException("Cannot map table name: " + tableName);
			}

			ResultSetSingleClassMapper mapper = new ResultSetSingleClassMapper(resultSetMappers.getNameMapping(), clazz);
			tableMaps.put(tableName, mapper);
		}

		ResultSetSingleClassMapper[] mapperArray = new ResultSetSingleClassMapper[tableMaps.values().size()];
		mapperArray = tableMaps.values().toArray(mapperArray);
		return new ResultSetMapping(mapperArray);
	}
}
