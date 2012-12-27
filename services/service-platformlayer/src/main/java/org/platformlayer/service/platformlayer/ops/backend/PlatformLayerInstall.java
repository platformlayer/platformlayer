package org.platformlayer.service.platformlayer.ops.backend;

import org.platformlayer.ops.Bound;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.DownloadFileByHash;
import org.platformlayer.ops.standardservice.StandardServiceInstall;
import org.platformlayer.ops.standardservice.StandardTemplateData;

public class PlatformLayerInstall extends StandardServiceInstall {

	@Bound
	PlatformLayerInstanceModel template;

	@Override
	protected StandardTemplateData getTemplate() {
		return template;
	}

	@Override
	protected DownloadFileByHash buildDownload() {
		DownloadFileByHash download = super.buildDownload();

		download.specifier = "platformlayer:production:platformlayer-xaas-webapp-1.0-SNAPSHOT-bin.tar.gz";

		return download;
	}

	@Override
	protected void addChildren() throws OpsException {
		super.addChildren();

		{
			DownloadFileByHash download = addChild(DownloadFileByHash.class);
			download.filePath = template.getRootWar();
			download.specifier = "gwt-platformlayerplus:production:gwt-platformlayer-1.0-SNAPSHOT.war";
		}

	}

}
