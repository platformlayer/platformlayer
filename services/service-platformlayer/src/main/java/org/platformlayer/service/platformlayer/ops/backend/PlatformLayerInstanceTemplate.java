package org.platformlayer.service.platformlayer.ops.backend;

import java.io.File;
import java.util.Map;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Property;
import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Command.Argument;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.java.JavaCommandBuilder;
import org.platformlayer.ops.standardservice.StandardTemplateData;
import org.platformlayer.service.platformlayer.model.PlatformLayerService;
import org.platformlayer.service.platformlayer.ops.auth.system.SystemAuthServiceController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public class PlatformLayerInstanceTemplate extends StandardTemplateData {

	private static final Logger log = LoggerFactory.getLogger(PlatformLayerInstanceTemplate.class);

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
			// Link to database
			links.addTarget(properties, "platformlayer", getDatabaseKey());
		}

		if (isMultitenant()) {
			properties.put("multitenant.keys", model.multitenantItems);
			properties.put("multitenant.project", getMultitenantProject());
			properties.put("multitenant.user", "master@" + model.dnsName);
			// properties.put("multitenant.password", model.multitenantPassword.plaintext());
			properties.put("multitenant.cert", getMultitenantKeyAlias());
		}

		{
			// Link to user auth
			links.addTarget(properties, "auth.user", model.auth);
		}

		{
			// Link to system auth (token validation)
			links.addTarget(properties, "auth.system", model.systemAuth);
		}

		if (model.config != null) {
			for (Property property : model.config.getProperties()) {
				properties.put(property.key, property.value);
			}
		}

		return properties;
	}

	String getSystemCertAlias() {
		return SystemAuthServiceController.CERT_NAME;
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

	@Override
	public String getDownloadSpecifier() {
		return "platformlayer:production:platformlayer-xaas-webapp-1.0-SNAPSHOT-bin.tar.gz";
	}

}
