package org.platformlayer.service.platformlayer.ops.backend;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.platformlayer.InetAddressChooser;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Property;
import org.platformlayer.core.model.Secret;
import org.platformlayer.core.model.Tag;
import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Command.Argument;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.databases.DatabaseHelper;
import org.platformlayer.ops.databases.DatabaseServer;
import org.platformlayer.ops.java.JavaCommandBuilder;
import org.platformlayer.ops.standardservice.StandardTemplateData;
import org.platformlayer.ops.uses.ConsumeHelpers;
import org.platformlayer.service.platformlayer.model.PlatformLayerDatabase;
import org.platformlayer.service.platformlayer.model.PlatformLayerService;
import org.platformlayer.service.platformlayer.model.SystemAuthService;
import org.platformlayer.service.platformlayer.model.UserAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class PlatformLayerInstanceTemplate extends StandardTemplateData {

	private static final Logger log = LoggerFactory.getLogger(PlatformLayerInstanceTemplate.class);

	@Inject
	DatabaseHelper databases;

	@Inject
	ConsumeHelpers links;

	@Bound
	PlatformLayerService model;

	@Override
	public PlatformLayerService getModel() {
		return model;
	}

	public File getLogConfigurationFile() {
		return new File(getInstanceDir(), "logback.xml");
	}

	@Override
	public Command getCommand() {
		JavaCommandBuilder command = new JavaCommandBuilder();
		command.addClasspathFolder(getInstallDir());
		command.addClasspathFolder(getServicesPath());

		command.setMainClass("org.platformlayer.xaas.web.StandaloneXaasWebserver");

		command.addDefine("conf", getConfigurationFile());

		command.addArgument(Argument.buildFile(getRootWar()));

		return command.get();
	}

	public File getRootWar() {
		return new File(getWarsPath(), "root.war");
	}

	public File getServicesPath() {
		return new File(getInstallDir(), "services");
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

	public SystemAuthService getSystemAuthService() throws OpsException {
		PlatformLayerKey systemAuthKey = getModel().systemAuth;
		SystemAuthService auth = platformLayer.getItem(systemAuthKey, SystemAuthService.class);
		return auth;
	}

	public UserAuthService getAuthService() throws OpsException {
		PlatformLayerKey authKey = getModel().auth;
		UserAuthService auth = platformLayer.getItem(authKey, UserAuthService.class);
		return auth;
	}

	public String getJdbcUrl() throws OpsException {
		PlatformLayerKey serverKey = getDatabase().server;

		ItemBase serverItem = (ItemBase) platformLayer.getItem(serverKey);
		DatabaseServer server = databases.toDatabase(serverItem);

		String jdbc = server.getJdbcUrl(getDatabaseName(), InetAddressChooser.preferIpv6());
		return jdbc;
	}

	public boolean isMultitenant() {
		return (!Strings.isNullOrEmpty(getModel().multitenantItems));
	}

	public String getMultitenantProject() {
		if (!isMultitenant()) {
			throw new IllegalStateException();
		}
		return "__master";
	}

	public String getMultitenantKeyAlias() {
		if (!isMultitenant()) {
			throw new IllegalStateException();
		}
		return "clientcert.project.master";
	}

	@Override
	protected Map<String, String> getConfigurationProperties() throws OpsException {
		Map<String, String> properties = Maps.newHashMap();

		PlatformLayerService model = getModel();

		properties.putAll(links.buildLinkTargetProperties(model.links));

		{
			// Configure keystore
			properties.put("keystore", getKeystoreFile().getAbsolutePath());
		}

		{
			// Configure database
			properties.put("platformlayer.jdbc.driverClassName", "org.postgresql.Driver");

			properties.put("platformlayer.jdbc.url", getJdbcUrl());
			properties.put("platformlayer.jdbc.username", getDatabaseUsername());
			properties.put("platformlayer.jdbc.password", getDatabasePassword().plaintext());
		}

		if (isMultitenant()) {
			properties.put("multitenant.keys", model.multitenantItems);
			properties.put("multitenant.project", getMultitenantProject());
			properties.put("multitenant.user", "master@" + model.dnsName);
			// properties.put("multitenant.password", model.multitenantPassword.plaintext());
			properties.put("multitenant.cert", getMultitenantKeyAlias());
		}

		{
			// Configure user auth
			List<String> userAuthKeys = Lists.newArrayList();

			UserAuthService userAuthService = getAuthService();
			String baseUrl = "https://" + userAuthService.dnsName + ":5001/";

			userAuthKeys.addAll(Tag.PUBLIC_KEY_SIG.find(userAuthService));
			Collections.sort(userAuthKeys); // Keep it stable

			properties.put("auth.user.ssl.keys", Joiner.on(',').join(userAuthKeys));
			properties.put("auth.user.url", baseUrl);

			// The ssl cert is actually multitenant.cert
		}

		{
			// Configure system auth (token validation)
			List<String> systemAuthKeys = Lists.newArrayList();

			SystemAuthService systemAuthService = getSystemAuthService();
			String systemAuthUrl = "https://" + systemAuthService.dnsName + ":35358/";

			systemAuthKeys.addAll(Tag.PUBLIC_KEY_SIG.find(systemAuthService));
			Collections.sort(systemAuthKeys); // Keep it stable

			properties.put("auth.system.ssl.keys", Joiner.on(',').join(systemAuthKeys));
			properties.put("auth.system.url", systemAuthUrl);
			properties.put("auth.system.ssl.cert", getSystemCertAlias());
		}

		if (model.config != null) {
			for (Property property : model.config.getProperties()) {
				properties.put(property.key, property.value);
			}
		}

		return properties;
	}

	String getSystemCertAlias() {
		return "clientcert.systemauth";
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

	@Override
	protected PlatformLayerKey getSslKeyPath() {
		return getModel().sslKey;
	}

}
