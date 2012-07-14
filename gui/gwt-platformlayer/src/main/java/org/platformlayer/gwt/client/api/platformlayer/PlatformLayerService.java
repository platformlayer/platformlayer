package org.platformlayer.gwt.client.api.platformlayer;

import org.platformlayer.gwt.client.api.login.Authentication;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface PlatformLayerService {
	void listRoots(Authentication token, AsyncCallback<UntypedItemCollection> callback);
}
