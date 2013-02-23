package org.platformlayer.jdbc;

import javax.sql.DataSource;

import com.google.inject.ImplementedBy;

@ImplementedBy(TomcatJdbcPoolDataSourceBuilder.class)
public interface DataSourceBuilder {
	DataSource buildDataSource(String key, JdbcConfiguration jdbcConfig);
}
