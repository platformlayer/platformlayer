package org.platformlayer.service.platformlayer.ops.auth.system;

import org.openstack.crypto.Md5Hash;
import org.platformlayer.ops.filesystem.DownloadFileByHash;
import org.platformlayer.ops.standardservice.StandardServiceInstall;
import org.platformlayer.ops.standardservice.StandardTemplateData;

public class SystemAuthInstall extends StandardServiceInstall {

	@Override
	protected StandardTemplateData getTemplate() {
		return injected(SystemAuthInstanceModel.class);
	}

	@Override
	protected DownloadFileByHash buildDownload() {
		DownloadFileByHash download = super.buildDownload();

		// TODO: Move to promoted build system

		// keystone-webapp-admin-1.0-SNAPSHOT-bin.tar.gz
		download.hash = new Md5Hash("076f063fde2a8abdeafcb9fcf5b615a7");

		return download;
	}
}
