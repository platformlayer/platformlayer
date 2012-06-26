package org.platformlayer.service.platformlayer.ops.backend;

import org.openstack.crypto.Md5Hash;
import org.platformlayer.ops.standardservice.StandardServiceInstall;
import org.platformlayer.ops.standardservice.StandardTemplateData;

public class PlatformLayerInstall extends StandardServiceInstall {
	@Override
	protected Md5Hash getMd5Hash() {
		Md5Hash hash = new Md5Hash("23340728c1f36cebe0180be76fd3e01b");
		return hash;
	}

	@Override
	protected StandardTemplateData getTemplate() {
		return injected(PlatformLayerInstanceModel.class);
	}
}
