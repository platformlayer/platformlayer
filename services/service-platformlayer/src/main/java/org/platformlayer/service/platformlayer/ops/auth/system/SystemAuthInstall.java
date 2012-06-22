package org.platformlayer.service.platformlayer.ops.auth.system;

import org.openstack.crypto.Md5Hash;
import org.platformlayer.ops.standardservice.StandardServiceInstall;
import org.platformlayer.ops.standardservice.StandardTemplateData;

public class SystemAuthInstall extends StandardServiceInstall {
	@Override
	protected Md5Hash getMd5Hash() {
		Md5Hash hash = new Md5Hash("12ac4f6850123ebd9564baf5aa1cab");
		return hash;
	}

	@Override
	protected StandardTemplateData getTemplate() {
		return injected(SystemAuthInstanceModel.class);
	}
}
