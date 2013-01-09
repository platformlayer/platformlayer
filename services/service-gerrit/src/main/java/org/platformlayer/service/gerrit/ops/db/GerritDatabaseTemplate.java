package org.platformlayer.service.gerrit.ops.db;

import javax.inject.Inject;

import org.platformlayer.InetAddressChooser;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Secret;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.databases.Database;
import org.platformlayer.ops.databases.DatabaseHelper;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.service.gerrit.model.GerritDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GerritDatabaseTemplate {

	private static final Logger log = LoggerFactory.getLogger(GerritDatabaseTemplate.class);

	public GerritDatabase getModel() {
		GerritDatabase model = OpsContext.get().getInstance(GerritDatabase.class);
		return model;
	}

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	DatabaseHelper databases;

	public String getDatabaseUsername() throws OpsException {
		return getModel().username;
	}

	public Secret getDatabasePassword() throws OpsException {
		return getModel().password;
	}

	public PlatformLayerKey getDatabaseServerKey() throws OpsException {
		PlatformLayerKey serverKey = getModel().server;
		return serverKey;
	}

	public String getDatabaseName() throws OpsException {
		return getModel().databaseName;
	}

	protected String getAuthJdbcUrl() throws OpsException {
		PlatformLayerKey serverKey = getModel().server;

		ItemBase serverItem = (ItemBase) platformLayer.getItem(serverKey);
		Database server = databases.toDatabase(serverItem);

		String jdbc = server.getJdbcUrl(serverItem, getDatabaseName(), InetAddressChooser.preferIpv6());
		return jdbc;
	}

}
