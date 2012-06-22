package org.platformlayer.service.platformlayer.ops.auth.user;

import java.io.File;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.java.JavaCommandBuilder;
import org.platformlayer.service.platformlayer.model.UserAuthService;
import org.platformlayer.service.platformlayer.ops.CommonTemplateData;

public class UserAuthInstanceModel extends CommonTemplateData {
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
		return "auth-system";
	}

	@Override
	protected Properties getConfigurationProperties() throws OpsException {
		Properties properties = super.getConfigurationProperties();

		properties.put("auth.user.module", "org.platformlayer.auth.keystone.KeystoneOpsUserModule");

		// sharedsecret=supersecret

		return properties;
	}

	public File getServicesDir() {
		return new File(getConfigDir(), "services");
	}

	@Override
	protected PlatformLayerKey getDatabaseKey() {
		return getModel().database;
	}

}
