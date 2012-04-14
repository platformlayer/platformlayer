package org.platformlayer.service.solr.ops;

import java.io.File;

import javax.inject.Inject;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.ManagedDirectory;
import org.platformlayer.ops.filesystem.ManagedSymlink;
import org.platformlayer.ops.filesystem.TemplatedFile;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.supervisor.SupervisorInstance;
import org.platformlayer.ops.tree.OpsTreeBase;

public class SolrInstance extends OpsTreeBase {
	@Inject
	PlatformLayerHelpers platformLayer;

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		SolrTemplateData template = injected(SolrTemplateData.class);

		// SolrServer model = template.getModel();
		String supervisorKey = "solr";

		File instanceDir = template.getInstanceDir();

		// Setup data dir
		{
			File dataDir = new File(instanceDir, "data");

			addChild(ManagedDirectory.build(dataDir, "755").setOwner("solr").setGroup("solr"));

			addChild(ManagedDirectory.build(new File(dataDir, "bin"), "755").setOwner("solr").setGroup("solr"));
			addChild(ManagedDirectory.build(new File(dataDir, "data"), "755").setOwner("solr").setGroup("solr"));

			addChild(TemplatedFile.build(template, new File(dataDir, "solr.xml")).setOwner("solr").setGroup("solr"));

			addChild(injected(SolrConfBoostrap.class));

			{
				SolrSchemaFile schema = injected(SolrSchemaFile.class);
				schema.filePath = new File(dataDir, "conf/schema.xml");
				addChild(schema);
			}
		}

		// Setup jetty
		{
			// We use the standalone configuration, because it's what solr tests with
			// Also, trying to get this to work under the Debian jetty was a nightmare
			// TODO: Support a different version of jetty?
			File etcDir = new File(instanceDir, "etc");
			addChild(ManagedDirectory.build(etcDir, "755").setOwner("solr").setGroup("solr"));

			addChild(TemplatedFile.build(template, new File(etcDir, "jetty.xml")).setOwner("solr").setGroup("solr"));
			addChild(ManagedSymlink.build(new File(etcDir, "webdefault.xml"), new File(template.getInstallDir(),
					"example/etc/webdefault.xml")));

			addChild(ManagedSymlink.build(new File(instanceDir, "lib"), new File(template.getInstallDir(),
					"example/lib")));
			addChild(ManagedSymlink.build(new File(instanceDir, "start.jar"), new File(template.getInstallDir(),
					"example/start.jar")));
			addChild(ManagedSymlink.build(new File(instanceDir, "webapps"), new File(template.getInstallDir(),
					"example/webapps")));
		}

		// TODO: Run in server mode
		// Turn down logging
		// Allocate more RAM?

		// TODO: Combine these three into one
		// Note: Don't use supervisord.conf, otherwise supervisorctl will fail with:
		// "Error: .ini file does not include supervisorctl section"
		addChild(TemplatedFile.build(template, new File(instanceDir, "supervisor.conf")).setFileMode("0444"));

		{
			ManagedSymlink symlink = ManagedSymlink.build(
					new File("/etc/supervisor/conf.d/" + supervisorKey + ".conf"), new File(instanceDir,
							"supervisor.conf"));
			addChild(symlink);
		}

		{
			SupervisorInstance service = injected(SupervisorInstance.class);
			service.id = supervisorKey;
			addChild(service);
		}

		{
			SolrCore core = injected(SolrCore.class);
			core.key = "core0";
			addChild(core);
		}
	}
}
