package org.platformlayer.ops.standardservice;

import java.io.File;
import java.util.Map;

import javax.inject.Inject;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.crypto.ManagedSecretKey;
import org.platformlayer.ops.helpers.ProviderHelper;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.metrics.MetricsManager;
import org.platformlayer.ops.templates.TemplateDataSource;

import com.google.common.collect.Maps;

public abstract class StandardTemplateData implements TemplateDataSource {

	@Inject
	protected ProviderHelper providers;

	@Inject
	protected PlatformLayerHelpers platformLayer;

	@Inject
	protected MetricsManager metricsManager;

	public abstract ItemBase getModel();

	public String getServiceKey() {
		return getKey() + "-" + getInstanceKey();
	}

	public String getUser() {
		return getKey();
	}

	public String getGroup() {
		return getKey();
	}

	public File getInstallDir() {
		return new File("/opt", getKey());
	}

	public File getInstanceDir() {
		return new File(new File("/var", getKey()), getInstanceKey());
	}

	public File getConfigDir() {
		return new File(getInstanceDir(), "config");
	}

	public abstract String getKey();

	public String getInstanceKey() {
		return "default";
	}

	protected abstract Command getCommand() throws OpsException;

	public File getConfigurationFile() {
		return new File(getConfigDir(), "configuration.properties");
	}

	protected abstract Map<String, String> getConfigurationProperties() throws OpsException;

	protected abstract PlatformLayerKey getSslKeyPath() throws OpsException;

	public ManagedSecretKey findSslKey() throws OpsException {
		PlatformLayerKey sslKey = getSslKeyPath();
		if (sslKey == null) {
			return null;
		}
		ItemBase sslKeyItem = (ItemBase) platformLayer.getItem(sslKey);
		return providers.toInterface(sslKeyItem, ManagedSecretKey.class);
	}

	public boolean shouldCreateSslKey() {
		return true;
	}

	public File getKeystoreFile() {
		return new File(getConfigDir(), "keystore.jks");
	}

	public File getDistFile() {
		return new File(getInstallDir(), getKey() + ".tar.gz");
	}

	public File getWarsPath() {
		return new File(getInstallDir(), "wars");
	}

	public boolean shouldExpand() {
		String distFilename = getDistFile().getName();
		return distFilename.endsWith(".tar.gz") || distFilename.endsWith(".zip");
	}

	public Map<String, String> getEnvironment() throws OpsException {
		return Maps.newHashMap();
	}

	public String getMatchExecutableName() {
		return null;
	}

	public void addMetricsProperties(Map<String, String> properties) {
		metricsManager.addConfigurationProperties(getModel().getKey(), properties);
	}

	public boolean shouldCreateKeystore() {
		return true;
	}

	public File getLogConfigurationFile() {
		return null;
	}

	// public String getDatabaseName() {
	// return "main";
	// }
}
