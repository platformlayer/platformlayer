package org.platformlayer.service.platformlayer.ops.auth;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.platformlayer.core.model.Link;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.standardservice.StandardTemplateData;
import org.platformlayer.ops.uses.LinkHelpers;

import com.google.common.collect.Lists;

public abstract class CommonAuthTemplateData extends StandardTemplateData {
	@Inject
	LinkHelpers links;

	@Override
	public void buildTemplateModel(Map<String, Object> model) throws OpsException {
	}

	protected abstract PlatformLayerKey getAuthDatabaseKey();

	public String getPlacementKey() {
		PlatformLayerKey databaseKey = getAuthDatabaseKey();
		return "platformlayer-" + databaseKey.getItemId().getKey();
	}

	@Override
	protected List<Link> getLinks() throws OpsException {
		List<Link> links = Lists.newArrayList();

		links.addAll(super.getLinks());

		{
			Link link = new Link();
			link.name = "auth";
			link.target = getAuthDatabaseKey();
			links.add(link);
		}

		return links;
	}

}
