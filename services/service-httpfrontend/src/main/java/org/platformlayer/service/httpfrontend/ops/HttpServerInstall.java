package org.platformlayer.service.httpfrontend.ops;

import java.io.File;

import org.openstack.crypto.Md5Hash;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.DownloadFileByHash;
import org.platformlayer.ops.filesystem.ExpandArchive;
import org.platformlayer.ops.filesystem.ManagedDirectory;
import org.platformlayer.ops.java.JavaVirtualMachine;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.ops.users.PosixGroup;
import org.platformlayer.ops.users.PosixUser;

public class HttpServerInstall extends OpsTreeBase {

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		HttpServerTemplateData template = injected(HttpServerTemplateData.class);

		File installDir = template.getInstallDir();

		addChild(ManagedDirectory.build(installDir, "0755"));

		{
			// TODO: Auto-update this?? Add JenkinsLatest?
			Md5Hash hash = new Md5Hash("354bdde5696312c5a36e1617985e1e83");

			File archiveFile = new File(installDir, "httpfrontend-server.tar.gz");

			// TODO: Cache?
			DownloadFileByHash download = addChild(DownloadFileByHash.class);
			download.hash = hash;
			download.filePath = archiveFile;

			ExpandArchive unzip = addChild(ExpandArchive.class);
			unzip.archiveFile = archiveFile;
			unzip.extractPath = installDir;
		}

		addChild(PackageDependency.build("supervisor"));

		// Allow binding to port 80 by non-root users
		// addChild(PackageDependency.build("libcap2-bin"));
		// Sadly this doesn't work (yet) so we run as root

		addChild(JavaVirtualMachine.buildJre7());

		{
			addChild(PosixGroup.build("http"));

			addChild(PosixUser.build("http", false, "http"));
		}
		// vm.addChild(CollectdCollector.build());

	}
}
