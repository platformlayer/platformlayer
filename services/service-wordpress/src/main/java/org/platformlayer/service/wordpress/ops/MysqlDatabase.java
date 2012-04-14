package org.platformlayer.service.wordpress.ops;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.service.mysql.ops.MysqlTarget;

public class MysqlDatabase {
	public String databaseName;

	@Handler
	public void handler(MysqlTarget mysql) throws OpsException {
		if (OpsContext.isConfigure()) {
			mysql.execute(String.format("CREATE DATABASE IF NOT EXISTS %s", databaseName));
		}
	}

}
