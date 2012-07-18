package org.platformlayer.service.dns.ops;

import java.io.File;

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

public class DnsServerInstall extends OpsTreeBase {

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		DnsServerTemplateData template = injected(DnsServerTemplateData.class);

		File installDir = template.getInstallDir();

		addChild(ManagedDirectory.build(installDir, "0755"));

		{
			File archiveFile = new File(installDir, "dns-server.tar.gz");

			// TODO: Cache?
			DownloadFileByHash download = addChild(DownloadFileByHash.class);
			download.specifier = "http-proxy:promote-production:proxy-0.1-SNAPSHOT-bin.tar.gz";
			download.filePath = archiveFile;

			ExpandArchive unzip = addChild(ExpandArchive.class);
			unzip.archiveFile = archiveFile;
			unzip.extractPath = installDir;
		}

		addChild(PackageDependency.build("supervisor"));

		// Allow binding to port 53 by non-root users
		// addChild(PackageDependency.build("libcap2-bin"));
		// Sadly this doesn't work (yet) so we run as root

		addChild(JavaVirtualMachine.buildJre7());

		{
			addChild(PosixGroup.build("dns"));

			addChild(PosixUser.build("dns", false, "dns"));
		}
		// vm.addChild(CollectdCollector.build());

	}
}
