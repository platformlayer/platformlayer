package org.platformlayer.service.httpfrontend.ops;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.UniqueTag;
import org.platformlayer.ops.tree.OwnedItem;
import org.platformlayer.service.httpfrontend.model.HttpSite;

public class OwnedHttpSite extends OwnedItem<HttpSite> {
	public String dnsName;
	public ItemBase model;

	@Override
	protected HttpSite buildItemTemplate() throws OpsException {
		Tag parentTag = Tag.buildParentTag(model.getKey());

		HttpSite httpSite = new HttpSite();
		httpSite.hostname = dnsName;
		httpSite.backend = model.getKey().getUrl();

		Tag uniqueTag = UniqueTag.build(model);
		httpSite.getTags().add(uniqueTag);
		httpSite.getTags().add(parentTag);

		httpSite.key = PlatformLayerKey.fromId(model.getId());

		return httpSite;
	}
}