package org.platformlayer.service.httpfrontend.ops;

import org.platformlayer.ops.Bound;
import org.platformlayer.ops.filesystem.DownloadFileByHash;
import org.platformlayer.ops.standardservice.StandardServiceInstall;
import org.platformlayer.ops.standardservice.StandardTemplateData;

public class HttpServerInstall extends StandardServiceInstall {

	@Bound
	HttpServerTemplateData template;

	@Override
	protected StandardTemplateData getTemplate() {
		return template;
	}

	@Override
	protected DownloadFileByHash buildDownload() {
		DownloadFileByHash download = super.buildDownload();

		// download.specifier = "http-proxy:promote-production:proxy-0.1-SNAPSHOT-bin.tar.gz";
		download.specifier = "platformlayerplus:production:http-proxy-1.0-SNAPSHOT-bin.tar.gz";

		return download;
	}

	// @Handler
	// public void handler() {
	// }
	//
	// @Override
	// protected void addChildren() throws OpsException {
	// HttpServerTemplateData template = injected(HttpServerTemplateData.class);
	//
	// File installDir = template.getInstallDir();
	//
	// addChild(ManagedDirectory.build(installDir, "0755"));
	//
	// {
	// File archiveFile = new File(installDir, "httpfrontend-server.tar.gz");
	//
	// // TODO: Cache?
	// DownloadFileByHash download = addChild(DownloadFileByHash.class);
	// download.specifier = "http-proxy:promote-production:proxy-0.1-SNAPSHOT-bin.tar.gz";
	// download.filePath = archiveFile;
	//
	// ExpandArchive unzip = addChild(ExpandArchive.class);
	// unzip.archiveFile = archiveFile;
	// unzip.extractPath = installDir;
	// }
	//
	// addChild(PackageDependency.build("supervisor"));
	//
	// // Allow binding to port 80 by non-root users
	// // addChild(PackageDependency.build("libcap2-bin"));
	// // Sadly this doesn't work (yet) so we run as root
	//
	// addChild(JavaVirtualMachine.buildJre7());
	//
	// {
	// addChild(PosixGroup.build("http"));
	//
	// addChild(PosixUser.build("http", false, "http"));
	// }
	// // vm.addChild(CollectdCollector.build());
	//
	// }
}
