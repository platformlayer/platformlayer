package org.platformlayer.ops.bootstrap;

import java.io.File;
import java.util.Map;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.TemplatedFile;
import org.platformlayer.ops.firewall.scripts.PersistIptablesScripts;
import org.platformlayer.ops.images.direct.PeerToPeerCopy;
import org.platformlayer.ops.packages.AptSourcesConfigurationFile.DefaultAptSourcesConfigurationFile;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.ops.templates.TemplateDataSource;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * General machine configuration.
 * 
 * Also used for LXC/KVM direct hosts.
 * 
 */
// TODO: Don't use for LXC/KVM hosts?
public class InstanceBootstrap extends OpsTreeBase {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(InstanceBootstrap.class);

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		addChild(PersistIptablesScripts.class);

		addChild(DefaultAptSourcesConfigurationFile.class);

		{
			File aptConfDir = new File("/etc/apt/apt.conf.d");

			TemplateDataSource template = new TemplateDataSource() {
				@Override
				public void buildTemplateModel(Map<String, Object> model) throws OpsException {
				}
			};
			addChild(TemplatedFile.build(template, new File(aptConfDir, "90-install-recommends")));
			addChild(TemplatedFile.build(template, new File(aptConfDir, "90-install-suggests")));
		}

		addChild(BootstrapLocales.class);

		addChild(ConfigureSshd.class);

		// We always install curl, because we use it to check for http proxy responsiveness
		// TODO: Switch to netcat, to avoid using curl here - it's quite big
		addChild(PackageDependency.build("curl"));

		PeerToPeerCopy peerToPeerCopy = Injection.getInstance(PeerToPeerCopy.class);
		peerToPeerCopy.addChildren(this);

		// if (OpsContext.isDelete()) {
		// OpenstackComputeMachine machine = OpsContext.get().getInstance(OpenstackComputeMachine.class);
		//
		// OpsTarget target = OpsContext.get().getInstance(OpsTarget.class);
		//
		// OpenstackInstance model = OpsContext.get().getInstance(OpenstackInstance.class);
		//
		// if (model.recipeId != null) {
		// DiskImageRecipe recipe = platformLayerClient.getItem(DiskImageRecipe.class, model.recipeId);
		//
		// diskImageRecipeHelper.applyRecipe(target, recipe);
		// }
		// }
	}
}
