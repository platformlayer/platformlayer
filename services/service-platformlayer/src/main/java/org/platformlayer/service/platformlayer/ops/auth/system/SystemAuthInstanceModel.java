package org.platformlayer.service.platformlayer.ops.auth.system;

import java.io.File;
import java.util.Map;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.java.JavaCommandBuilder;
import org.platformlayer.service.platformlayer.model.SystemAuthService;
import org.platformlayer.service.platformlayer.ops.auth.CommonAuthTemplateData;

public class SystemAuthInstanceModel extends CommonAuthTemplateData {
	static final Logger log = Logger.getLogger(SystemAuthInstanceModel.class);

	public SystemAuthService getModel() {
		SystemAuthService model = OpsContext.get().getInstance(SystemAuthService.class);
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
		command.setMainClass("org.openstack.keystone.server.KeystoneAdminServer");

		command.addDefine("conf", getConfigurationFile());

		return command.get();
	}

	@Override
	public String getKey() {
		return "auth-system";
	}

	@Override
	protected Map<String, String> getConfigurationProperties() throws OpsException {
		Map<String, String> properties = super.getConfigurationProperties();

		properties.put("auth.system.module", "org.platformlayer.auth.keystone.KeystoneOpsSystemModule");
		properties.put("sharedsecret", getModel().tokenSecret.plaintext());

		return properties;
	}

	@Override
	protected PlatformLayerKey getAuthDatabaseKey() {
		return getModel().database;
	}

	@Override
	protected PlatformLayerKey getSslKeyPath() {
		return getModel().sslKey;
	}

}
