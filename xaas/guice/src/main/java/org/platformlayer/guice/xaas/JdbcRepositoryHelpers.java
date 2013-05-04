package org.platformlayer.guice.xaas;

import java.sql.SQLException;

import org.platformlayer.ids.ItemType;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.jdbc.AtomHelpers;

import com.fathomdb.jdbc.JdbcConnection;

public class JdbcRepositoryHelpers {
	@Deprecated
	// Use atoms
	public static int getServiceKey(JdbcConnection connection, ServiceType service) throws SQLException {
		return AtomHelpers.getCode(connection, "services", service.getKey());
	}

	@Deprecated
	// Use atoms
	public static int getMetadataKeyId(JdbcConnection connection, String metadataKey) throws SQLException {
		return AtomHelpers.getCode(connection, "metadata_keys", metadataKey);
	}

	@Deprecated
	// Use atoms
	public static int getProjectKey(JdbcConnection connection, ProjectId project) throws SQLException {
		return AtomHelpers.getCode(connection, "project_codes", project.getKey());
	}

	@Deprecated
	// Use atoms
	public static int getItemTypeKey(JdbcConnection connection, ItemType itemType) throws SQLException {
		return AtomHelpers.getCode(connection, "item_types", itemType.getKey());
	}

}
