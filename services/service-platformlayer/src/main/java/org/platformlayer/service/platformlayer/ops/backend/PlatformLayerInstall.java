package org.platformlayer.service.platformlayer.ops.backend;

import org.openstack.crypto.Md5Hash;
import org.platformlayer.ops.filesystem.DownloadFileByHash;
import org.platformlayer.ops.standardservice.StandardServiceInstall;
import org.platformlayer.ops.standardservice.StandardTemplateData;

public class PlatformLayerInstall extends StandardServiceInstall {

	@Override
	protected StandardTemplateData getTemplate() {
		return injected(PlatformLayerInstanceModel.class);
	}

	@Override
	protected DownloadFileByHash buildDownload() {
		DownloadFileByHash download = super.buildDownload();

		// TODO: Move to promoted build system

		// platformlayer-xaas-webapp-1.0-SNAPSHOT-bin.tar.gz
		download.hash = new Md5Hash("b6f9d3a17403a85a397f40fc47701b5b");

		return download;
	}
}
