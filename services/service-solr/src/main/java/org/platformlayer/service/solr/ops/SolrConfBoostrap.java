package org.platformlayer.service.solr.ops;

import java.io.File;

import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.tree.OpsTreeBase;

public class SolrConfBoostrap extends OpsTreeBase {
	@Handler
	public void handler(OpsTarget target) throws OpsException {
		SolrTemplateData template = injected(SolrTemplateData.class);

		File instanceDir = template.getInstanceDir();
		File dataDir = new File(instanceDir, "data");
		File confDir = new File(dataDir, "conf");

		if (OpsContext.isConfigure()) {
			if (target.getFilesystemInfoFile(confDir) == null) {
				File exampleConfDir = new File(template.getInstallDir(), "example/solr/conf/");

				Command copy = Command.build("cp -r {0} {1}", exampleConfDir, dataDir);
				target.executeCommand(copy);

				target.chown(confDir, "solr", "solr", true, false);
			}
		}
	}

	@Override
	protected void addChildren() throws OpsException {

	}
}
