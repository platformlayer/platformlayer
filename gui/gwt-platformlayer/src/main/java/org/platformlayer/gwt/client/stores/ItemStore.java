package org.platformlayer.gwt.client.stores;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.platformlayer.gwt.client.api.platformlayer.OpsProject;
import org.platformlayer.gwt.client.api.platformlayer.PlatformLayerService;
import org.platformlayer.gwt.client.api.platformlayer.UntypedItem;

import com.google.gwt.user.client.rpc.AsyncCallback;

@Singleton
public class ItemStore {
	static final Logger log = Logger.getLogger(ItemStore.class.getName());

	@Inject
	PlatformLayerService platformLayer;

	public void getItem(OpsProject project, String key, final AsyncCallback<UntypedItem> callback) {
		platformLayer.getItem(project, key, new AsyncCallback<UntypedItem>() {
			@Override
			public void onSuccess(UntypedItem result) {
				callback.onSuccess(result);
			}

			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

}
