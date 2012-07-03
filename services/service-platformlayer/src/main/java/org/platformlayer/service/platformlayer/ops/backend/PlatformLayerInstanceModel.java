package org.platformlayer.service.platformlayer.ops.backend;

import java.io.File;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Secret;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.databases.Database;
import org.platformlayer.ops.databases.DatabaseHelper;
import org.platformlayer.ops.java.JavaCommandBuilder;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.standardservice.StandardTemplateData;
import org.platformlayer.service.platformlayer.model.PlatformLayerDatabase;
import org.platformlayer.service.platformlayer.model.PlatformLayerService;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public class PlatformLayerInstanceModel extends StandardTemplateData {
	static final Logger log = Logger.getLogger(PlatformLayerInstanceModel.class);

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	DatabaseHelper databases;

	public PlatformLayerService getModel() {
		PlatformLayerService model = OpsContext.get().getInstance(PlatformLayerService.class);
		return model;
	}

	public File getLogConfigurationFile() {
		return new File(getInstanceDir(), "logback.xml");
	}

	@Override
	public Command getCommand() {
		JavaCommandBuilder command = new JavaCommandBuilder();
		command.addClasspathFolder(getInstallDir());
		command.setMainClass("org.platformlayer.xaas.web.StandaloneXaasWebserver");

		command.addDefine("conf", getConfigurationFile());

		return command.get();
	}

	@Override
	public String getKey() {
		return "platformlayer";
	}

	public String getDatabaseUsername() throws OpsException {
		return getDatabase().username;
	}

	public Secret getDatabasePassword() throws OpsException {
		return getDatabase().password;
		// return Secret.build("platformlayer-password");
	}

	public String getDatabaseName() throws OpsException {
		return getDatabase().databaseName;
	}

	public PlatformLayerKey getDatabaseServerKey() throws OpsException {
		PlatformLayerKey serverKey = getDatabase().server;
		return serverKey;
	}

	public PlatformLayerDatabase getDatabase() throws OpsException {
		PlatformLayerKey databaseKey = getDatabaseKey();
		PlatformLayerDatabase database = platformLayer.getItem(databaseKey, PlatformLayerDatabase.class);
		return database;
	}

	public String getJdbcUrl() throws OpsException {
		PlatformLayerKey serverKey = getDatabase().server;

		ItemBase serverItem = (ItemBase) platformLayer.getItem(serverKey);
		Database server = databases.toDatabase(serverItem);

		String jdbc = server.getJdbcUrl(serverItem, getDatabaseName());
		return jdbc;
	}

	@Override
	protected Map<String, String> getConfigurationProperties() throws OpsException {
		Map<String, String> properties = Maps.newHashMap();

		properties.put("platformlayer.jdbc.driverClassName", "org.postgresql.Driver");

		properties.put("platformlayer.jdbc.url", getJdbcUrl());
		properties.put("platformlayer.jdbc.username", getDatabaseUsername());
		properties.put("platformlayer.jdbc.password", getDatabasePassword().plaintext());

		String multitenantItems = getModel().multitenantItems;

		if (!Strings.isNullOrEmpty(multitenantItems)) {
			properties.put("multitenant.keys", multitenantItems);
			properties.put("multitenant.project", "__master");
			properties.put("multitenant.user", "__master");
			properties.put("multitenant.password", getMasterPassword().plaintext());
		}

		return properties;
	}

	private Secret getMasterPassword() {
		return getModel().multitenantPassword;
	}

	protected PlatformLayerKey getDatabaseKey() {
		return getModel().database;
	}

	@Override
	public void buildTemplateModel(Map<String, Object> model) throws OpsException {
	}

	public String getPlacementKey() {
		PlatformLayerKey databaseKey = getDatabaseKey();
		return "platformlayer-" + databaseKey.getItemId().getKey();
	}
}
