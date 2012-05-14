package org.platformlayer.service.postgresql.ops;

import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.service.postgresql.model.PostgresqlServer;

public class PostgresqlServerBootstrap {

	@Handler
	public void handler() throws OpsException {
		PostgresqlServer model = OpsContext.get().getInstance(PostgresqlServer.class);
		OpsTarget target = OpsContext.get().getInstance(OpsTarget.class);

		// su postgres -c "psql --command=\"ALTER USER postgres WITH PASSWORD 'secret';\""

		String password = model.rootPassword.plaintext();

		String sql = String.format("ALTER USER postgres WITH PASSWORD '%s';", password);
		String psql = String.format("psql --command=\"%s\"", sql);
		Command command = Command.build("su postgres -c {0}", psql);

		target.executeCommand(command);
	}

	public static PostgresqlServerBootstrap build() {
		return Injection.getInstance(PostgresqlServerBootstrap.class);
	}

}
