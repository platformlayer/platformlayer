package org.platformlayer.service.platformlayer.ops.auth.db;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.service.platformlayer.model.PlatformLayerAuthDatabase;
import org.platformlayer.service.platformlayer.ops.auth.CommonAuthTemplateData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlatformLayerAuthDatabaseTemplate extends CommonAuthTemplateData {

	private static final Logger log = LoggerFactory.getLogger(PlatformLayerAuthDatabaseTemplate.class);

	@Override
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

	@Override
	protected PlatformLayerKey getSslKeyPath() {
		return null;
	}

}
