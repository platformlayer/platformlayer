package org.platformlayer.service.httpfrontend.ops;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.http.HttpManager;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.ops.tree.OwnedItem;

public class HttpFrontendManager implements HttpManager {

	@Override
	public OwnedItem<?> addHttpSite(OpsTreeBase parent, ItemBase model, String dnsName) throws OpsException {
		OwnedHttpSite site = parent.addChild(OwnedHttpSite.class);
		site.dnsName = dnsName;
		site.model = model;
		return site;
	}

}
