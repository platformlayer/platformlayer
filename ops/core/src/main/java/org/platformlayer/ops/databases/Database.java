package org.platformlayer.ops.databases;

import org.platformlayer.core.model.Secret;
import org.platformlayer.ops.OpsException;

public interface Database {
	String getJdbcUrl(Object db, String database) throws OpsException;

	Secret getRootPassword(Object db);

	String getRootUsername(Object db);

	DatabaseTarget buildDatabaseTarget(Object db, String username, Secret password, String databaseName)
			throws OpsException;
}
