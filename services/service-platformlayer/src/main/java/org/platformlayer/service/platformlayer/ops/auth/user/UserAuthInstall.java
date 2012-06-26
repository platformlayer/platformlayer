package org.platformlayer.service.platformlayer.ops.auth.user;

import org.openstack.crypto.Md5Hash;
import org.platformlayer.ops.standardservice.StandardServiceInstall;
import org.platformlayer.ops.standardservice.StandardTemplateData;

public class UserAuthInstall extends StandardServiceInstall {
	@Override
	protected Md5Hash getMd5Hash() {
		Md5Hash hash = new Md5Hash("79b8e3187cc2ef5bbe84701fa64b5dc6");
		return hash;
	}

	@Override
	protected StandardTemplateData getTemplate() {
		return injected(UserAuthInstanceModel.class);
	}
}
