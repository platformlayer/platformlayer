package org.platformlayer.ops.bootstrap;

import java.io.File;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.SimpleFile;
import org.platformlayer.ops.firewall.scripts.PersistIptablesScripts;
import org.platformlayer.ops.packages.AptSourcesConfigurationFile.DefaultAptSourcesConfigurationFile;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.ops.tree.OpsTreeBase;

public class InstanceBootstrap extends OpsTreeBase {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(InstanceBootstrap.class);

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		addChild(PersistIptablesScripts.class);

		addChild(DefaultAptSourcesConfigurationFile.class);

		addChild(BootstrapLocales.class);

		addChild(SimpleFile.build(getClass(), new File("/etc/ssh/sshd_config")).setFileMode("0644").setOwner("root")
				.setGroup("root").setUpdateAction(Command.build("service ssh reload")));

		// We always install curl, because we use it to check for http proxy responsiveness
		// TODO: Switch to netcat, to avoid using curl here - it's quite big
		addChild(PackageDependency.build("curl"));

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
