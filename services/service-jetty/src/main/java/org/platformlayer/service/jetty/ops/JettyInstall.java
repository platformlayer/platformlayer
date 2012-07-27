package org.platformlayer.service.jetty.ops;

import org.openstack.crypto.Md5Hash;
import org.platformlayer.ops.Bound;
import org.platformlayer.ops.filesystem.DownloadFileByHash;
import org.platformlayer.ops.standardservice.StandardServiceInstall;
import org.platformlayer.ops.standardservice.StandardTemplateData;
import org.platformlayer.service.jetty.model.JettyService;

public class JettyInstall extends StandardServiceInstall {

	@Bound
	JettyService model;

	@Override
	protected StandardTemplateData getTemplate() {
		return injected(JettyTemplate.class);
	}

	@Override
	protected DownloadFileByHash buildDownload() {
		DownloadFileByHash download = super.buildDownload();

		String url = "http://download.eclipse.org/jetty/stable-7/dist/jetty-distribution-7.6.5.v20120716.tar.gz";

		download.setUrl(url);
		download.hash = new Md5Hash("1f09d1e2ddc029ae8c8ce2361a6800f8");

		return download;
	}

}
