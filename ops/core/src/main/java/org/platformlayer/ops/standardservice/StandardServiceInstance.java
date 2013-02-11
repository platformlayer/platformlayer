package org.platformlayer.ops.standardservice;

import java.io.File;
import java.util.Map;

import javax.inject.Inject;

import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsProvider;
import org.platformlayer.ops.crypto.ManagedKeystore;
import org.platformlayer.ops.filesystem.ManagedDirectory;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.metrics.MetricsInstance;
import org.platformlayer.ops.metrics.MetricsManager;
import org.platformlayer.ops.supervisor.StandardService;
import org.platformlayer.ops.tree.OpsTreeBase;

import com.google.inject.util.Providers;

public abstract class StandardServiceInstance extends OpsTreeBase {
	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	MetricsManager metricsManager;

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		final StandardTemplateData template = getTemplate();

		File instanceDir = template.getInstanceDir();

		addChild(MetricsInstance.class);

		String user = template.getUser();
		String group = template.getGroup();

		addChild(ManagedDirectory.build(instanceDir, "0700").setOwner(user).setGroup(group));
		addChild(ManagedDirectory.build(template.getConfigDir(), "0700").setOwner(user).setGroup(group));

		addConfigurationFile(template);

		addExtraFiles();

		{
			StandardService service = addChild(StandardService.class);

			Command command = template.getCommand();
			service.command = OpsProvider.of(command);

			Map<String, String> env = template.getEnvironment();
			service.environment = Providers.of(env);

			service.instanceDir = instanceDir;
			service.key = template.getServiceKey();

			service.owner = template.getModel().getKey();

			service.matchExecutableName = template.getMatchExecutableName();
		}

		if (template.shouldCreateKeystore()) {
			ManagedDirectory configDir = findDirectory(template.getConfigDir());

			File keystoreFile = template.getKeystoreFile();

			if (template.shouldCreateSslKey()) {
				ManagedKeystore httpsKey = configDir.addChild(ManagedKeystore.class);
				httpsKey.path = keystoreFile;
				httpsKey.tagWithPublicKeys = template.getModel();
				httpsKey.alias = ManagedKeystore.DEFAULT_WEBSERVER_ALIAS;
				httpsKey.key = template.findSslKey();
			}

			addExtraKeys(configDir, keystoreFile);
		}
	}

	protected void addExtraKeys(OpsTreeBase parent, File keystoreFile) throws OpsException {
	}

	/**
	 * Used for things that need to be configured before the service
	 */
	protected void addExtraFiles() throws OpsException {

	}

	protected void addConfigurationFile(final StandardTemplateData template) throws OpsException {
		PropertiesConfigFile conf = addChild(PropertiesConfigFile.class);
		conf.filePath = template.getConfigurationFile();
		conf.propertiesSupplier = new OpsProvider<Map<String, String>>() {
			@Override
			public Map<String, String> get() throws OpsException {
				Map<String, String> properties = template.getConfigurationProperties();

				template.addMetricsProperties(properties);

				return properties;
			}
		};
	}

	protected abstract StandardTemplateData getTemplate();
}
