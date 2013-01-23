package org.platformlayer.service.platformlayer.ops.backend;

import java.io.File;

import org.platformlayer.ops.Bound;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.DownloadFileByHash;
import org.platformlayer.ops.filesystem.ExpandArchive;
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

		addService("service-extensions");
		addService("service-domains");
	}

	private void addService(String key) throws OpsException {
		DownloadFileByHash download = addChild(DownloadFileByHash.class);
		download.filePath = new File(template.getServicesPath(), key + ".tar.gz");
		download.specifier = "platformlayerplus:production:" + key + "-1.0-SNAPSHOT-service-package.tar.gz";

		// TODO: Only unzip if newly downloaded
		ExpandArchive unzip = addChild(ExpandArchive.class);
		unzip.archiveFile = download.filePath;
		unzip.extractPath = template.getServicesPath();
	}
}
