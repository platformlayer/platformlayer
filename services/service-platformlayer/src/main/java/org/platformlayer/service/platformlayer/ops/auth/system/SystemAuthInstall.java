package org.platformlayer.service.platformlayer.ops.auth.system;

import org.platformlayer.ops.Bound;
import org.platformlayer.ops.filesystem.DownloadFileByHash;
import org.platformlayer.ops.standardservice.StandardServiceInstall;
import org.platformlayer.ops.standardservice.StandardTemplateData;

public class SystemAuthInstall extends StandardServiceInstall {

	@Bound
	SystemAuthInstanceModel template;

	@Override
	protected StandardTemplateData getTemplate() {
		return template;
	}

	@Override
	protected DownloadFileByHash buildDownload() {
		DownloadFileByHash download = super.buildDownload();

		download.specifier = "platformlayer:production:keystone-webapp-admin-1.0-SNAPSHOT-bin.tar.gz";

		return download;
	}
}
