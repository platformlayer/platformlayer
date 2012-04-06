package org.platformlayer.guice.xaas;

import java.sql.Connection;
import java.sql.SQLException;

import org.platformlayer.ids.ItemType;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.jdbc.AtomHelpers;

public class JdbcRepositoryHelpers {
    @Deprecated
    // Use atoms
    public static int getServiceKey(Connection connection, ServiceType service) throws SQLException {
        return AtomHelpers.getKey(connection, "services", service.getKey());
    }

    @Deprecated
    // Use atoms
    public static int getProjectKey(Connection connection, ProjectId project) throws SQLException {
        return AtomHelpers.getKey(connection, "projects", project.getKey());
    }

    @Deprecated
    // Use atoms
    public static int getMetadataKeyId(Connection connection, String metadataKey) throws SQLException {
        return AtomHelpers.getKey(connection, "metadata_keys", metadataKey);
    }

    @Deprecated
    // Use atoms
    public static int getItemTypeKey(Connection connection, ItemType itemType) throws SQLException {
        return AtomHelpers.getKey(connection, "item_types", itemType.getKey());
    }

}
