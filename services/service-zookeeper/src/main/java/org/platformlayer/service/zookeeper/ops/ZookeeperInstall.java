package org.platformlayer.service.zookeeper.ops;

import org.openstack.crypto.Md5Hash;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.filesystem.DownloadFileByHash;
import org.platformlayer.ops.standardservice.StandardServiceInstall;

public class ZookeeperInstall extends StandardServiceInstall {

	@Override
	protected ZookeeperInstanceModel getTemplate() {
		return injected(ZookeeperInstanceModel.class);
	}

	@Override
	protected DownloadFileByHash buildDownload() {
		DownloadFileByHash download = super.buildDownload();

		// TODO: Would be nice not to hard code this mirror
		String apacheMirror = "http://ftp.osuosl.org/pub/apache/";

		// This probably does need to be hard-coded though
		// (though maybe selectable from a list of supported releases)
		String url = apacheMirror + "zookeeper/zookeeper-3.3.5/zookeeper-3.3.5.tar.gz";

		download.setUrl(url);
		download.hash = new Md5Hash("4c2c969bce8717d6443e184ff91dfdc7");

		return download;
	}

	@Override
	@Handler
	public void handler() {
	}

}
