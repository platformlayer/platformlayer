package org.platformlayer.service.platformlayer.ops.auth.system;

import org.openstack.crypto.Md5Hash;
import org.platformlayer.ops.standardservice.StandardServiceInstall;
import org.platformlayer.ops.standardservice.StandardTemplateData;

public class SystemAuthInstall extends StandardServiceInstall {
	@Override
	protected Md5Hash getMd5Hash() {
		// keystone-webapp-admin-1.0-SNAPSHOT-bin.tar.gz
		Md5Hash hash = new Md5Hash("076f063fde2a8abdeafcb9fcf5b615a7");
		return hash;
	}

	@Override
	protected StandardTemplateData getTemplate() {
		return injected(SystemAuthInstanceModel.class);
	}
}
