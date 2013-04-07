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
}
