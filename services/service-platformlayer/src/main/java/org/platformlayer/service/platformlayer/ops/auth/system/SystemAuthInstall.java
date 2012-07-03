package org.platformlayer.service.platformlayer.ops.auth.system;

import org.openstack.crypto.Md5Hash;
import org.platformlayer.ops.standardservice.StandardServiceInstall;
import org.platformlayer.ops.standardservice.StandardTemplateData;

public class SystemAuthInstall extends StandardServiceInstall {
	@Override
	protected Md5Hash getMd5Hash() {
		// keystone-webapp-admin-1.0-SNAPSHOT-bin.tar.gz
		Md5Hash hash = new Md5Hash("4380eb6de77a80d50e8918c92d9b28d1");
		return hash;
	}

	@Override
	protected StandardTemplateData getTemplate() {
		return injected(SystemAuthInstanceModel.class);
	}
}
