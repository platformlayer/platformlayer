package org.platformlayer.service.platformlayer.ops.auth.user;

import java.io.File;

import org.platformlayer.ops.Bound;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.DownloadFileByHash;
import org.platformlayer.ops.standardservice.StandardServiceInstall;
import org.platformlayer.ops.standardservice.StandardTemplateData;

public class UserAuthInstall extends StandardServiceInstall {

	@Bound
	UserAuthInstanceTemplate template;

	@Override
	protected StandardTemplateData getTemplate() {
		return template;
	}

	@Override
	protected void addChildren() throws OpsException {
		super.addChildren();

		for (String extension : template.getExtensions()) {
			String key = extension;

			DownloadFileByHash download = addChild(DownloadFileByHash.class);
			download.filePath = new File(template.getExtensionsPath(), key + ".jar");
			download.specifier = "platformlayerplus:production:" + key + "-1.0-SNAPSHOT.jar";
		}
	}
}
