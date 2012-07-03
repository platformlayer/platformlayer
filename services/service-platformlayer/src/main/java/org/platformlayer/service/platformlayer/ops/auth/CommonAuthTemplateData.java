package org.platformlayer.service.platformlayer.ops.auth;

import java.util.Map;

import javax.inject.Inject;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Secret;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.databases.Database;
import org.platformlayer.ops.databases.DatabaseHelper;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.standardservice.StandardTemplateData;
import org.platformlayer.service.platformlayer.model.PlatformLayerAuthDatabase;

import com.google.common.collect.Maps;

public abstract class CommonAuthTemplateData extends StandardTemplateData {

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	InstanceHelpers instanceHelpers;

	@Inject
	DatabaseHelper databases;

	@Override
	public void buildTemplateModel(Map<String, Object> model) throws OpsException {
		// model.put("jdbcUrl", getJdbcUrl());
		// model.put("jdbcUsername", getDatabaseUsername());
		// model.put("jdbcPassword", getDatabasePassword().plaintext());
	}

	public String getAuthDatabaseUsername() throws OpsException {
		return getAuthDatabase().username;
	}

	public Secret getAuthDatabasePassword() throws OpsException {
		return getAuthDatabase().password;
		// return Secret.build("platformlayer-password");
	}

	public PlatformLayerKey getAuthDatabaseServerKey() throws OpsException {
		PlatformLayerKey serverKey = getAuthDatabase().server;
		return serverKey;
	}

	public PlatformLayerAuthDatabase getAuthDatabase() throws OpsException {
		PlatformLayerKey authDatabaseKey = getAuthDatabaseKey();
		PlatformLayerAuthDatabase authDatabase = platformLayer
				.getItem(authDatabaseKey, PlatformLayerAuthDatabase.class);
		return authDatabase;
	}

	public String getAuthDatabaseName() throws OpsException {
		return getAuthDatabase().databaseName;
	}

	protected abstract PlatformLayerKey getAuthDatabaseKey();

	protected String getAuthJdbcUrl() throws OpsException {
		PlatformLayerKey serverKey = getAuthDatabase().server;

		ItemBase serverItem = (ItemBase) platformLayer.getItem(serverKey);
		Database server = databases.toDatabase(serverItem);

		String jdbc = server.getJdbcUrl(serverItem, getAuthDatabaseName());
		return jdbc;
	}

	@Override
	protected Map<String, String> getConfigurationProperties() throws OpsException {
		Map<String, String> properties = Maps.newHashMap();
		properties.put("auth.jdbc.driverClassName", "org.postgresql.Driver");

		properties.put("auth.jdbc.url", getAuthJdbcUrl());
		properties.put("auth.jdbc.username", getAuthDatabaseUsername());
		properties.put("auth.jdbc.password", getAuthDatabasePassword().plaintext());

		return properties;
	}

	public String getPlacementKey() {
		PlatformLayerKey databaseKey = getAuthDatabaseKey();
		return "platformlayer-" + databaseKey.getItemId().getKey();
	}

}
