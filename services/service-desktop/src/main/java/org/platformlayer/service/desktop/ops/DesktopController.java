package org.platformlayer.service.desktop.ops;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.instances.DiskImageRecipeBuilder;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.networks.PublicEndpoint;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.desktop.model.Desktop;
import org.platformlayer.service.imagefactory.v1.OperatingSystemRecipe;

public class DesktopController extends OpsTreeBase {
	static final Logger log = Logger.getLogger(DesktopController.class);

	@Handler
	public void doOperation() throws OpsException, IOException {
	}

	@Override
	protected void addChildren() throws OpsException {
		Desktop model = OpsContext.get().getInstance(Desktop.class);

		InstanceBuilder instance = InstanceBuilder.build(model.dnsName,
				DiskImageRecipeBuilder.buildDiskImageRecipe(this));
		instance.publicPorts.add(22);

		instance.hostPolicy.allowRunInContainer = true;
		instance.minimumMemoryMb = 4096;

		addChild(instance);

		{
			RecipeOperatingSystem os = injected(RecipeOperatingSystem.class);
			os.operatingSystem = new OperatingSystemRecipe();
			os.operatingSystem.setDistribution("debian");
			os.operatingSystem.setVersion("wheezy");
			instance.addChild(os);
		}

		// We use curl for backups
		instance.addChild(PackageDependency.build("curl"));

		{
			PublicEndpoint endpoint = injected(PublicEndpoint.class);
			// endpoint.network = null;
			endpoint.publicPort = 22;
			endpoint.backendPort = 22;
			endpoint.dnsName = model.dnsName;

			endpoint.tagItem = OpsSystem.toKey(model);
			endpoint.parentItem = OpsSystem.toKey(model);

			instance.addChild(endpoint);
		}

		// {
		// BackupDirectory backup = injected(BackupDirectory.class);
		// backup.itemKey = model.getKey();
		//
		// File jenkinsRoot = new File("/var/lib/jenkins");
		// backup.backupRoot = jenkinsRoot;
		//
		// String[] excludes = { "jobs/*/workspace", "jobs/*/modules", "jobs/*/builds/*/workspace.tar.gz",
		// ".m2/repository" };
		//
		// for (String exclude : excludes) {
		// backup.excludes.add(new File(jenkinsRoot, exclude));
		// }
		//
		// instance.addChild(backup);
		// }
	}
}
