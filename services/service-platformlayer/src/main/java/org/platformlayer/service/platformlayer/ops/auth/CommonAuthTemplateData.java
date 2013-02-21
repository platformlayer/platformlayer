package org.platformlayer.service.platformlayer.ops.auth;

import java.util.Map;

import javax.inject.Inject;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.standardservice.StandardTemplateData;
import org.platformlayer.ops.uses.LinkHelpers;
import org.platformlayer.ops.uses.LinkTarget;
import org.platformlayer.service.platformlayer.model.PlatformLayerAuthDatabase;

import com.google.common.collect.Maps;

public abstract class CommonAuthTemplateData extends StandardTemplateData {
	@Inject
	LinkHelpers links;

	@Override
	public void buildTemplateModel(Map<String, Object> model) throws OpsException {
	}

	public LinkTarget getAuthDatabase() throws OpsException {
		PlatformLayerKey authDatabaseKey = getAuthDatabaseKey();
		PlatformLayerAuthDatabase authDatabase = platformLayer
				.getItem(authDatabaseKey, PlatformLayerAuthDatabase.class);
		LinkTarget dbTarget = providers.toInterface(authDatabase, LinkTarget.class);
		return dbTarget;
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
