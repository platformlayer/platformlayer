package org.platformlayer.service.jetty.ops;

import org.platformlayer.ops.Bound;
import org.platformlayer.ops.filesystem.DownloadFileByHash;
import org.platformlayer.ops.standardservice.StandardServiceInstall;
import org.platformlayer.ops.standardservice.StandardTemplateData;

import com.fathomdb.hash.Md5Hash;

public class JettyInstall extends StandardServiceInstall {

	@Bound
	public JettyTemplate template;

	@Override
	protected StandardTemplateData getTemplate() {
		return template;
	}

	@Override
	protected DownloadFileByHash buildDownload() {
		DownloadFileByHash download = super.buildDownload();

		download.setUrl("http://mirrors.med.harvard.edu/eclipse/jetty/7.6.8.v20121106/dist/jetty-distribution-7.6.8.v20121106.tar.gz");
		download.hash = new Md5Hash("49daf27ae78ec1188e23cd489a68bc3b");

		return download;
	}

}
