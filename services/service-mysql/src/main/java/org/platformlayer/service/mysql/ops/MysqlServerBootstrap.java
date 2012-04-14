package org.platformlayer.service.mysql.ops;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.service.mysql.model.MysqlServer;

public class MysqlServerBootstrap {

	@Handler
	public void handler() throws OpsException {
		MysqlServer model = OpsContext.get().getInstance(MysqlServer.class);

		MysqlTarget mysql = new MysqlTarget("localhost", "root", model.rootPassword);

		// TODO: Better Idempotency
		// Save password into text file??

		if (!mysql.canLogin()) {
			mysql = new MysqlTarget("localhost", "root", MysqlServerController.DEFAULT_BOOTSTRAP_PASSWORD);

			// Clean up the users table
			mysql.execute("delete from mysql.user where user='';");
			mysql.execute("update mysql.user set host='%' where user='root' and host='localhost';");
			mysql.execute("delete from mysql.user where user='root' and host<>'%';");
			mysql.execute("flush privileges;");

			mysql.changePassword(model.rootPassword);
		}
	}

	public static MysqlServerBootstrap build() {
		return Injection.getInstance(MysqlServerBootstrap.class);
	}
}
