package org.platformlayer.service.network.ops;

import javax.inject.Inject;
import javax.inject.Provider;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.tree.OpsTreeBase;

public class EnsureItem extends OpsTreeBase {

	public Provider<ItemBase> modelProvider;

	public static EnsureItem build(Provider<ItemBase> model) {
		EnsureItem item = injected(EnsureItem.class);
		item.modelProvider = model;
		return item;
	}

	@Override
	protected void addChildren() throws OpsException {

	}

	@Inject
	PlatformLayerHelpers client;

	@Handler
	public void handler() throws OpsException {
		client.putItem(modelProvider.get());
	}
}
