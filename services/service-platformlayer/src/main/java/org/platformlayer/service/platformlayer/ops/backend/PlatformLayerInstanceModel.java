package org.platformlayer.service.platformlayer.ops.backend;

import java.io.File;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.java.JavaCommandBuilder;
import org.platformlayer.service.platformlayer.model.PlatformLayerService;
import org.platformlayer.service.platformlayer.ops.CommonTemplateData;

public class PlatformLayerInstanceModel extends CommonTemplateData {
	static final Logger log = Logger.getLogger(PlatformLayerInstanceModel.class);

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

		return command.get();
	}

	@Override
	public String getKey() {
		return "platformlayer";
	}

	@Override
	protected Properties getConfigurationProperties() throws OpsException {
		Properties properties = new Properties();
		return properties;
	}

	@Override
	protected PlatformLayerKey getDatabaseKey() {
		return getModel().database;
	}

}
