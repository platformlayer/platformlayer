package org.platformlayer.service.platformlayer.ops.authdb;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.service.platformlayer.model.PlatformLayerAuthDatabase;
import org.platformlayer.service.platformlayer.ops.CommonTemplateData;

public class PlatformLayerAuthDatabaseTemplate extends CommonTemplateData {
	static final Logger log = Logger.getLogger(PlatformLayerAuthDatabaseTemplate.class);

	public PlatformLayerAuthDatabase getModel() {
		PlatformLayerAuthDatabase model = OpsContext.get().getInstance(PlatformLayerAuthDatabase.class);
		return model;
	}

	@Override
	public Command getCommand() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getKey() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected PlatformLayerKey getAuthDatabaseKey() {
		return getModel().getKey();
	}

}
