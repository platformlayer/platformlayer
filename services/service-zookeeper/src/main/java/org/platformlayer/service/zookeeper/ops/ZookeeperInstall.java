package org.platformlayer.service.zookeeper.ops;

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

public class ZookeeperInstall extends OpsTreeBase {

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		addChild(JavaVirtualMachine.buildJava6());

		addChild(injected(SupervisordService.class));

		{
			// TODO: Would be nice not to hard code this mirror
			String apacheMirror = "http://ftp.osuosl.org/pub/apache/";

			// This probably does need to be hard-coded though
			// (though maybe selectable from a list of supported releases)
			String file = "zookeeper/zookeeper-3.3.5/zookeeper-3.3.5.tar.gz";
			Md5Hash hash = new Md5Hash("4c2c969bce8717d6443e184ff91dfdc7");

			File basePath = new File("/opt/zookeeper/");
			File zipFile = new File(basePath, "zookeeper-3.3.5.tar.gz");
			File extractPath = new File(basePath, "zookeeper-3.3.5");

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
	}
}
