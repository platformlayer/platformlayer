package org.platformlayer.service.database.ops;

import java.security.cert.X509Certificate;
import java.util.Map;

import javax.inject.Inject;

import org.platformlayer.InetAddressChooser;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.databases.DatabaseServer;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.helpers.ProviderHelper;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.postgres.CreateDatabase;
import org.platformlayer.ops.postgres.CreateUser;
import org.platformlayer.ops.postgres.DatabaseConnection;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.ops.uses.LinkTarget;
import org.platformlayer.service.database.model.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.crypto.OpenSshUtils;
import com.google.common.collect.Maps;

public class DatabaseController extends OpsTreeBase implements LinkTarget {
	private static final Logger log = LoggerFactory.getLogger(DatabaseController.class);

	@Bound
	Database model;

	@Inject
	InstanceHelpers instanceHelpers;

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	ProviderHelper providers;

	@Handler
	public void handler() throws OpsException {
	}

	@Override
	protected void addChildren() throws OpsException {
		DatabaseConnection dbConnection;
		{
			dbConnection = addChild(DatabaseConnection.build(model.server));

			dbConnection.databaseName = model.databaseName;
		}

		{
			CreateDatabase db = dbConnection.addChild(CreateDatabase.class);
			db.databaseName = model.databaseName;
		}

		{
			CreateUser db = dbConnection.addChild(CreateUser.class);
			db.grantDatabaseName = model.databaseName;
			db.databaseUser = model.username;
			db.databasePassword = model.password;
		}
	}

	@Override
	public Map<String, String> buildLinkTargetConfiguration(InetAddressChooser inetAddressChooser) throws OpsException {
		Map<String, String> config = Maps.newHashMap();
		config.put("jdbc.driverClassName", "org.postgresql.Driver");

		ItemBase serverItem = platformLayer.getItem(model.server);
		DatabaseServer databaseServer = providers.toInterface(serverItem, DatabaseServer.class);

		String jdbcUrl = databaseServer.getJdbcUrl(model.databaseName, inetAddressChooser);

		config.put("jdbc.url", jdbcUrl);
		config.put("jdbc.username", model.username);
		config.put("jdbc.password", model.password.plaintext());

		X509Certificate sslCertificate = databaseServer.getCertificate();

		boolean useSsl = (sslCertificate != null);
		config.put("jdbc.ssl", String.valueOf(useSsl));

		if (useSsl) {
			String sigString = OpenSshUtils.getSignatureString(sslCertificate.getPublicKey());
			config.put("jdbc.ssl.keys", sigString);
		}

		return config;
	}
}
