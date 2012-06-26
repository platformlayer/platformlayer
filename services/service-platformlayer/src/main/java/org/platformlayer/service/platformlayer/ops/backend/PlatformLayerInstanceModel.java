package org.platformlayer.service.platformlayer.ops.backend;

import java.io.File;
import java.util.Map;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Secret;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.java.JavaCommandBuilder;
import org.platformlayer.service.platformlayer.model.PlatformLayerService;
import org.platformlayer.service.platformlayer.ops.CommonTemplateData;

import com.google.common.base.Strings;

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
	protected Map<String, String> getConfigurationProperties() throws OpsException {
		Map<String, String> properties = super.getConfigurationProperties();

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

	@Override
	protected PlatformLayerKey getAuthDatabaseKey() {
		return getModel().database;
	}

}
