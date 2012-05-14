package org.platformlayer.service.solr.ops;

import java.io.File;

import org.platformlayer.crypto.Md5Hash;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.DownloadFile;
import org.platformlayer.ops.filesystem.ExpandArchive;
import org.platformlayer.ops.java.JavaVirtualMachine;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.ops.supervisor.SupervisordService;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.ops.users.PosixGroup;
import org.platformlayer.ops.users.PosixUser;

public class SolrInstall extends OpsTreeBase {

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		addChild(JavaVirtualMachine.buildJava6());

		// addChild(injected(JettyInstall.class));

		addChild(injected(SupervisordService.class));

		{
			// TODO: Would be nice not to hard code this mirror
			String apacheMirror = "http://apache.osuosl.org/";

			// This probably does need to be hard-coded though
			// (though maybe selectable from a list of supported releases)
			String file = "lucene/solr/3.6.0/apache-solr-3.6.0.tgz";
			Md5Hash hash = new Md5Hash("ac11ef4408bb015aa3a5eefcb1047aec");

			File basePath = new File("/opt/");
			File zipFile = new File(basePath, "apache-solr-3.6.0.tgz");
			File extractPath = new File(basePath, "apache-solr-3.6.0");

			DownloadFile download = injected(DownloadFile.class);
			download.setUrl(apacheMirror + file);
			download.hash = hash;
			download.filePath = zipFile;
			addChild(download);

			// Needed for ExpandArchive
			addChild(PackageDependency.build("unzip"));

			// TODO: Only unzip if newly downloaded
			ExpandArchive unzip = injected(ExpandArchive.class);
			unzip.archiveFile = zipFile;
			unzip.extractPath = extractPath;
			addChild(unzip);
		}

		addChild(PosixGroup.build("solr"));
		addChild(PosixUser.build("solr", "solr"));
	}
}
