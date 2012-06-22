package org.platformlayer.ops.standardservice;

import java.io.File;
import java.util.Properties;

import org.platformlayer.ops.Command;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.templates.TemplateDataSource;

public abstract class StandardTemplateData implements TemplateDataSource {

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

	protected abstract Command getCommand();

	public File getConfigurationFile() {
		return new File(getConfigDir(), "configuration.properties");
	}

	protected abstract Properties getConfigurationProperties() throws OpsException;
}
