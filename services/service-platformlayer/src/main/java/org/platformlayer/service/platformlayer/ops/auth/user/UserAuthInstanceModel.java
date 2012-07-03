package org.platformlayer.service.platformlayer.ops.auth.user;

import java.io.File;
import java.util.Map;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.java.JavaCommandBuilder;
import org.platformlayer.service.platformlayer.model.UserAuthService;
import org.platformlayer.service.platformlayer.ops.auth.CommonAuthTemplateData;

public class UserAuthInstanceModel extends CommonAuthTemplateData {
	static final Logger log = Logger.getLogger(UserAuthInstanceModel.class);

	public UserAuthService getModel() {
		UserAuthService model = OpsContext.get().getInstance(UserAuthService.class);
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

}
