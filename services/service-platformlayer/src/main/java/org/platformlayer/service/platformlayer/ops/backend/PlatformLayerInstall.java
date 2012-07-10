package org.platformlayer.service.platformlayer.ops.backend;

import org.openstack.crypto.Md5Hash;
import org.platformlayer.ops.standardservice.StandardServiceInstall;
import org.platformlayer.ops.standardservice.StandardTemplateData;

public class PlatformLayerInstall extends StandardServiceInstall {
	@Override
	protected Md5Hash getMd5Hash() {
		// platformlayer-xaas-webapp-1.0-SNAPSHOT-bin.tar.gz
		Md5Hash hash = new Md5Hash("0b89621dac1004fee55edf0866329e10");
		return hash;
	}

	@Override
	protected StandardTemplateData getTemplate() {
		return injected(PlatformLayerInstanceModel.class);
	}
}
