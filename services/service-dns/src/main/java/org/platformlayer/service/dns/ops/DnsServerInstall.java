package org.platformlayer.service.dns.ops;

import org.platformlayer.ops.Bound;
import org.platformlayer.ops.filesystem.DownloadFileByHash;
import org.platformlayer.ops.standardservice.StandardServiceInstall;

public class DnsServerInstall extends StandardServiceInstall {

	@Bound
	DnsServerTemplate template;

	@Override
	protected DnsServerTemplate getTemplate() {
		return template;
	}

	@Override
	protected DownloadFileByHash buildDownload() {
		DownloadFileByHash download = super.buildDownload();

		download.specifier = "http-proxy:promote-production:proxy-0.1-SNAPSHOT-bin.tar.gz";

		return download;
	}
}
