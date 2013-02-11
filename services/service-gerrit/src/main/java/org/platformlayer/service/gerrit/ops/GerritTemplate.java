package org.platformlayer.service.gerrit.ops;

import java.io.File;
import java.util.Map;

import javax.inject.Inject;

import org.platformlayer.InetAddressChooser;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Secret;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.databases.DatabaseHelper;
import org.platformlayer.ops.databases.DatabaseServer;
import org.platformlayer.service.gerrit.model.GerritDatabase;
import org.platformlayer.service.gerrit.model.GerritService;
import org.platformlayer.service.jetty.ops.JettyTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class GerritTemplate extends JettyTemplate {

	private static final Logger log = LoggerFactory.getLogger(GerritTemplate.class);

	@Inject
	DatabaseHelper databases;

	@Override
	public GerritService getModel() {
		return getGerritService();
	}

	public GerritService getGerritService() {
		GerritService model = OpsContext.get().getInstance(GerritService.class);
		return model;
	}

	public File getLogConfigurationFile() {
		return new File(getInstanceDir(), "logback.xml");
	}

	// @Override
	// public Command getCommand() {
	// // GerritCodeReview -jar /var/gerrit/default/data/bin/gerrit.war daemon -d /var/gerrit/default/data
	// // --console-log
	//
	// File gerritScript = new File(getDataDir(), "bin/gerrit.sh");
	// Command command = Command.build("{0} run", gerritScript);
	//
	// return command;
	// }

	@Override
	public String getKey() {
		return "gerrit";
	}

	public File getServicesDir() {
		return new File(getConfigDir(), "services");
	}

	public String getDatabaseUsername() throws OpsException {
		return getDatabase().username;
	}

	public String getDatabaseName() throws OpsException {
		return getDatabase().databaseName;
	}

	public Secret getDatabasePassword() throws OpsException {
		return getDatabase().password;
		// return Secret.build("platformlayer-password");
	}

	public GerritDatabase getDatabase() throws OpsException {
		PlatformLayerKey databaseKey = getDatabaseKey();
		GerritDatabase database = platformLayer.getItem(databaseKey, GerritDatabase.class);
		return database;
	}

	protected PlatformLayerKey getDatabaseKey() {
		return getGerritService().database;
	}

	protected String getJdbcUrl() throws OpsException {
		PlatformLayerKey serverKey = getDatabase().server;

		ItemBase serverItem = (ItemBase) platformLayer.getItem(serverKey);
		DatabaseServer server = databases.toDatabase(serverItem);

		String jdbc = server.getJdbcUrl(getDatabaseName(), InetAddressChooser.preferIpv6());
		return jdbc;
	}

	@Override
	protected PlatformLayerKey getSslKeyPath() {
		return getModel().sslKey;
	}

	public String getPlacementKey() {
		return "gerrit";
	}

	@Override
	public void buildTemplateModel(Map<String, Object> model) throws OpsException {
		super.buildTemplateModel(model);

		Map<String, String> config = getConfigurationProperties();
		// We can't have dots, or freemarker assumes they are objects
		for (String key : config.keySet()) {
			String value = config.get(key);
			key = key.replace(".", "_");
			model.put(key, value);
		}

		String requiredRole = "project-" + getModel().getKey().getProjectString();
		model.put("requiredRole", requiredRole);
	}

	public int getSshdPort() {
		return 29418;
	}

	public int getWebPort() {
		return 443;
	}

	@Override
	public File getConfigurationFile() {
		return new File(getDataDir(), "etc/gerrit.config");
	}

	boolean useOpenId = false;

	@Override
	protected Map<String, String> getConfigurationProperties() throws OpsException {
		Map<String, String> model = Maps.newHashMap();

		model.put("database.type", "POSTGRESQL");
		// model.put("database.hostname", );
		model.put("database.url", getJdbcUrl());
		model.put("database.username", getDatabaseUsername());
		model.put("database.password", getDatabasePassword().plaintext());
		model.put("database.database", getDatabaseName());

		String scheme = "https";

		model.put("gerrit.basePath", "git");
		model.put("gerrit.canonicalWebUrl", scheme + "://" + getModel().dnsName + ":" + getWebPort() + "/");

		if (useOpenId) {
			model.put("auth.type", "OPENID");
		} else {
			model.put("auth.type", "HTTP");
			model.put("auth.emailFormat", "{0}");

			// LDAP...
			// [auth]
			// type = LDAP_BIND
			// [ldap]
			// server = ldap://1.2.3.4:11389
			// accountEmailAddress = "mail"
			// accountBase = "ou=people,dc=example,dc=com"

		}
		model.put("sendemail.enable", "false");

		model.put("container.user", getUser());
		model.put("container.javaHome", "/usr/lib/jvm/java-7-openjdk-amd64/jre");

		model.put("sshd.listenAddress", "*:" + getSshdPort());

		String listenUrl = scheme + "://*:" + getWebPort();
		model.put("httpd.listenUrl", listenUrl);

		model.put("cache.directory", "cache");

		// [gerrit]
		// basePath = git
		// canonicalWebUrl = http://devstack:8080/
		// [database]
		// type = POSTGRESQL
		// #database = db/ReviewDB
		// [auth]
		// type = OPENID
		// [sendemail]
		// smtpServer = localhost
		// [container]
		// user = justinsb
		// javaHome = /usr/lib/jvm/java-6-openjdk-amd64/jre
		// [sshd]
		// listenAddress = *:29418
		// [httpd]
		// listenUrl = http://*:8080/
		// [cache]
		// directory = cache}

		return model;
	}

	public File getDataDir() {
		return new File(getInstanceDir(), "data");
	}

	public File getInstallWarFile() {
		// return new File(getInstallDir(), "gerrit-2.4.2.war");
		return new File(getInstallDir(), "gerrit-full-2.5.war");
	}

	@Override
	public String getMatchExecutableName() {
		// We run a script, but that script runs java
		return "java";
	}

	public File getLibExtDir() {
		return new File(getJettyInstanceDir(), "lib/ext");
	}

	public File getJettyInstanceDir() {
		return getInstanceDir();
	}

	@Override
	protected boolean getUseHttps() {
		return true;
	}

	@Override
	protected boolean getUseJndi() {
		return true;
	}

}
