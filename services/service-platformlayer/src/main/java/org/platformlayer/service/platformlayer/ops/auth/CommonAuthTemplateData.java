package org.platformlayer.service.platformlayer.ops.auth;

import java.util.Map;

import javax.inject.Inject;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.standardservice.StandardTemplateData;
import org.platformlayer.ops.uses.LinkHelpers;

import com.google.common.collect.Maps;

public abstract class CommonAuthTemplateData extends StandardTemplateData {
	@Inject
	LinkHelpers links;

	@Override
	public void buildTemplateModel(Map<String, Object> model) throws OpsException {
	}

	protected abstract PlatformLayerKey getAuthDatabaseKey();

	@Override
	protected Map<String, String> getConfigurationProperties() throws OpsException {
		Map<String, String> properties = Maps.newHashMap();

		{
			links.addTarget(properties, "auth.", getAuthDatabaseKey());
		}

		return properties;
	}

	public String getPlacementKey() {
		PlatformLayerKey databaseKey = getAuthDatabaseKey();
		return "platformlayer-" + databaseKey.getItemId().getKey();
	}

}
