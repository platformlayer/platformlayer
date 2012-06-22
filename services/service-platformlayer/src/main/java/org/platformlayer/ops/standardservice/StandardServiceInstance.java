package org.platformlayer.ops.standardservice;

import java.io.File;
import java.util.Properties;

import javax.inject.Inject;

import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
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

		{
			PropertiesConfigFile conf = addChild(PropertiesConfigFile.class);
			conf.filePath = template.getConfigurationFile();
			conf.propertiesSupplier = new Supplier<Properties>() {

				@Override
				public Properties get() {
					try {
						return template.getConfigurationProperties();
					} catch (OpsException e) {
						throw new IllegalStateException("Error building configuration", e);
					}
				}
			};
		}

		{
			StandardService service = addChild(StandardService.class);
			Command command = template.getCommand();

			service.command = Providers.of(command);
			service.instanceDir = instanceDir;
			service.key = template.getServiceKey();
		}

	}

	protected abstract StandardTemplateData getTemplate();
}
