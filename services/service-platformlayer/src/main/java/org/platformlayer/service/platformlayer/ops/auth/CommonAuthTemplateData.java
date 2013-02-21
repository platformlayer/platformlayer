package org.platformlayer.service.platformlayer.ops.auth;

import java.util.Map;

import javax.inject.Inject;

import org.platformlayer.InetAddressChooser;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.databases.DatabaseHelper;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.standardservice.StandardTemplateData;
import org.platformlayer.ops.uses.LinkTarget;
import org.platformlayer.service.platformlayer.model.PlatformLayerAuthDatabase;

import com.fathomdb.properties.PropertyUtils;
import com.google.common.collect.Maps;

public abstract class CommonAuthTemplateData extends StandardTemplateData {

	@Inject
	InstanceHelpers instanceHelpers;

	@Inject
	DatabaseHelper databases;

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
			// TODO: Use a link?
			LinkTarget authDatabase = getAuthDatabase();

			InetAddressChooser inetAddressChooser = InetAddressChooser.preferIpv6();

			Map<String, String> config = authDatabase.buildLinkTargetConfiguration(inetAddressChooser);
			config = PropertyUtils.prefixProperties(config, "auth.");

			properties.putAll(config);
		}

		return properties;
	}

	public String getPlacementKey() {
		PlatformLayerKey databaseKey = getAuthDatabaseKey();
		return "platformlayer-" + databaseKey.getItemId().getKey();
	}

}
