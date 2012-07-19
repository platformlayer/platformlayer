package org.platformlayer.service.platformlayer.ops.auth.user;

import org.openstack.crypto.Md5Hash;
import org.platformlayer.ops.filesystem.DownloadFileByHash;
import org.platformlayer.ops.standardservice.StandardServiceInstall;
import org.platformlayer.ops.standardservice.StandardTemplateData;

public class UserAuthInstall extends StandardServiceInstall {

	@Override
	protected DownloadFileByHash buildDownload() {
		DownloadFileByHash download = super.buildDownload();

		// TODO: Move to promoted build system

		// keystone-webapp-user-1.0-SNAPSHOT-bin.tar.gz
		download.hash = new Md5Hash("ad289ff3bcde3b2e983acdeff0917dc9");

		return download;
	}

	@Override
	protected StandardTemplateData getTemplate() {
		return injected(UserAuthInstanceModel.class);
	}
}
