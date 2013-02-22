package org.platformlayer.service.zookeeper.ops;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.filesystem.DownloadFileByHash;
import org.platformlayer.ops.standardservice.StandardServiceInstall;

import com.fathomdb.hash.Md5Hash;

public class ZookeeperInstall extends StandardServiceInstall {

	@Override
	protected ZookeeperInstanceModel getTemplate() {
		return injected(ZookeeperInstanceModel.class);
	}

	@Override
	protected DownloadFileByHash buildDownload() {
		DownloadFileByHash download = super.buildDownload();

		// TODO: Would be nice not to hard code this mirror
		// String apacheMirror = "http://ftp.osuosl.org/pub/apache/";
		String apacheMirror = "http://apache.osuosl.org/";

		// This probably does need to be hard-coded though
		// (though maybe selectable from a list of supported releases)

		// String url = apacheMirror + "zookeeper/zookeeper-3.3.5/zookeeper-3.3.5.tar.gz";
		// download.setUrl(url);
		// download.hash = new Md5Hash("4c2c969bce8717d6443e184ff91dfdc7");

		String url = apacheMirror + "zookeeper/zookeeper-3.4.5/zookeeper-3.4.5.tar.gz";
		download.setUrl(url);
		download.hash = new Md5Hash("f64fef86c0bf2e5e0484d19425b22dcb");

		return download;
	}

	@Override
	@Handler
	public void handler() {
	}

}
