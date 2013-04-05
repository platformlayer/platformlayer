package org.platformlayer.service.platformlayer.ops.auth.user;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.java.JavaCommandBuilder;
import org.platformlayer.ops.uses.LinkHelpers;
import org.platformlayer.service.platformlayer.model.UserAuthService;
import org.platformlayer.service.platformlayer.ops.auth.CommonAuthTemplateData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserAuthInstanceTemplate extends CommonAuthTemplateData {

	private static final Logger log = LoggerFactory.getLogger(UserAuthInstanceTemplate.class);

	@Bound
	UserAuthService model;

	@Inject
	LinkHelpers links;

	@Override
	public UserAuthService getModel() {
		return model;
	}

	public File getLogConfigurationFile() {
		return new File(getInstanceDir(), "logback.xml");
	}

	@Override
	public Command getCommand() {
		JavaCommandBuilder command = new JavaCommandBuilder();
		command.addClasspathFolder(getInstallDir());
		// command.addDefine("logback.configurationFile", getLogConfigurationFile());
		command.setMainClass("org.openstack.keystone.server.KeystoneUserServer");

		command.addDefine("conf", getConfigurationFile());

		return command.get();
	}

	@Override
	public String getKey() {
		return "auth-user";
	}

	@Override
	protected Map<String, String> getConfigurationProperties() throws OpsException {
		Map<String, String> properties = super.getConfigurationProperties();

		properties.put("auth.user.module", "org.platformlayer.auth.keystone.KeystoneOpsUserModule");
		properties.put("sharedsecret", getModel().tokenSecret.plaintext());

		return properties;
	}

	public File getServicesDir() {
		return new File(getConfigDir(), "services");
	}

	@Override
	protected PlatformLayerKey getAuthDatabaseKey() {
		return getModel().database;
	}

	@Override
	protected PlatformLayerKey getSslKeyPath() {
		return getModel().sslKey;
	}

	@Override
	public String getDownloadSpecifier() {
		return "platformlayer:production:keystone-webapp-user-1.0-SNAPSHOT-bin.tar.gz";
	}

}
