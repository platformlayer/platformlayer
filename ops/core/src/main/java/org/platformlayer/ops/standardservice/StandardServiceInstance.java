package org.platformlayer.ops.standardservice;

import java.io.File;
import java.util.Map;

import javax.inject.Inject;

import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.crypto.ManagedKeystore;
import org.platformlayer.ops.filesystem.ManagedDirectory;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.supervisor.StandardService;
import org.platformlayer.ops.tree.OpsTreeBase;

import com.google.common.base.Supplier;
import com.google.inject.util.Providers;

public abstract class StandardServiceInstance extends OpsTreeBase {
	@Inject
	PlatformLayerHelpers platformLayer;

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		final StandardTemplateData template = getTemplate();
		// SystemAuthService model = template.getModel();

		// int port = template.getPort();

		File instanceDir = template.getInstanceDir();
		// File installDir = template.getInstallDir();

		String user = template.getUser();
		String group = template.getGroup();

		addChild(ManagedDirectory.build(instanceDir, "0700").setOwner(user).setGroup(group));
		addChild(ManagedDirectory.build(template.getConfigDir(), "0700").setOwner(user).setGroup(group));

		addConfigurationFile(template);

		addExtraFiles();

		{
			StandardService service = addChild(StandardService.class);
			Command command = template.getCommand();

			service.command = Providers.of(command);
			service.instanceDir = instanceDir;
			service.key = template.getServiceKey();
		}

		if (template.shouldCreateSslKey()) {
			ManagedDirectory configDir = findDirectory(template.getConfigDir());

			File keystoreFile = template.getKeystoreFile();

			{
				ManagedKeystore httpsKey = configDir.addChild(ManagedKeystore.class);
				httpsKey.path = keystoreFile;
				httpsKey.tagWithPublicKeys = template.getModel();
				httpsKey.alias = ManagedKeystore.DEFAULT_WEBSERVER_ALIAS;
				httpsKey.key = template.findSslKey();
			}
		}
	}

	/**
	 * Used for things that need to be configured before the service
	 */
	protected void addExtraFiles() throws OpsException {

	}

	protected void addConfigurationFile(final StandardTemplateData template) throws OpsException {
		PropertiesConfigFile conf = addChild(PropertiesConfigFile.class);
		conf.filePath = template.getConfigurationFile();
		conf.propertiesSupplier = new Supplier<Map<String, String>>() {

			@Override
			public Map<String, String> get() {
				try {
					return template.getConfigurationProperties();
				} catch (OpsException e) {
					throw new IllegalStateException("Error building configuration", e);
				}
			}
		};
	}

	protected abstract StandardTemplateData getTemplate();
}
