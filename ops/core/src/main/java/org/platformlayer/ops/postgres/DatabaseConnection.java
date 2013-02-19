package org.platformlayer.ops.postgres;

import javax.inject.Inject;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Secret;
import org.platformlayer.ops.BindingScope;
import org.platformlayer.ops.CustomRecursor;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OperationRecursor;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.databases.DatabaseServer;
import org.platformlayer.ops.databases.DatabaseTarget;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.helpers.ProviderHelper;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.tree.OpsTreeBase;

public class DatabaseConnection extends OpsTreeBase implements CustomRecursor {
	public static final int POSTGRES_PORT = 5432;

	public PlatformLayerKey serverKey;
	public String username;
	public Secret password;
	public String databaseName;

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	InstanceHelpers instanceHelpers;

	@Inject
	ProviderHelper providers;

	public static DatabaseConnection build(PlatformLayerKey serverKey) throws OpsException {
		if (serverKey == null) {
			throw new IllegalArgumentException("serverKey cannot be null");
		}
		DatabaseConnection db = injected(DatabaseConnection.class);
		db.serverKey = serverKey;
		return db;
	}

	@Handler
	public void handler() {
	}

	@Override
	public void doRecurseOperation() throws OpsException {
		ItemBase server = platformLayer.getItem(serverKey);

		DatabaseServer database = providers.toInterface(server, DatabaseServer.class);

		String username = this.username;
		if (username == null) {
			username = database.getRootUsername();
		}

		if (username.equals("postgres") && password == null) {
			password = database.getRootPassword();
		}

		DatabaseTarget dbTarget = database.buildDatabaseTarget(username, password, databaseName);
		BindingScope scope = BindingScope.push(dbTarget);
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
