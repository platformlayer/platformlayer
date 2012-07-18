package org.platformlayer.service.platformlayer.ops.backend;

import org.openstack.crypto.Md5Hash;
import org.platformlayer.ops.standardservice.StandardServiceInstall;
import org.platformlayer.ops.standardservice.StandardTemplateData;

public class PlatformLayerInstall extends StandardServiceInstall {
	@Override
	protected Md5Hash getMd5Hash() {
		// platformlayer-xaas-webapp-1.0-SNAPSHOT-bin.tar.gz
		Md5Hash hash = new Md5Hash("b6f9d3a17403a85a397f40fc47701b5b");
		return hash;
	}

	@Override
	protected StandardTemplateData getTemplate() {
		return injected(PlatformLayerInstanceModel.class);
	}
}
