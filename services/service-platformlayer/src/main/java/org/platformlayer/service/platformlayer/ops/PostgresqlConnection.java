package org.platformlayer.service.platformlayer.ops;

import javax.inject.Inject;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Secret;
import org.platformlayer.ops.BindingScope;
import org.platformlayer.ops.CustomRecursor;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OperationRecursor;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.postgresql.model.PostgresqlServer;

public class PostgresqlConnection extends OpsTreeBase implements CustomRecursor {
	public static final int POSTGRES_PORT = 5432;

	public PlatformLayerKey key;
	public String username;
	public Secret password;

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	InstanceHelpers instanceHelpers;

	public static PostgresqlConnection build(PlatformLayerKey key) {
		PostgresqlConnection mysql = injected(PostgresqlConnection.class);
		mysql.key = key;
		return mysql;
	}

	@Handler
	public void handler() {
	}

	@Override
	public void doRecurseOperation() throws OpsException {
		PostgresqlServer pgServer = platformLayer.getItem(key, PostgresqlServer.class);

		String username = this.username;
		if (username == null) {
			username = "postgres";
		}

		if (username.equals("postgres") && password == null) {
			password = pgServer.rootPassword;
		}

		Machine machine = instanceHelpers.getMachine(pgServer);

		String address = machine.getAddress(NetworkPoint.forTargetInContext(), POSTGRES_PORT);
		PostgresTarget mysql = new PostgresTarget(address, username, password);

		BindingScope scope = BindingScope.push(mysql);
		try {
			OpsContext opsContext = OpsContext.get();
			OperationRecursor.doRecurseChildren(opsContext, this);
		} finally {
			scope.pop();
		}
	}

	@Override
	protected void addChildren() throws OpsException {

	}

}
