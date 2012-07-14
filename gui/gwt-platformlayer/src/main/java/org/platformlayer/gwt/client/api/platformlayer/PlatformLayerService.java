package org.platformlayer.gwt.client.api.platformlayer;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface PlatformLayerService {
	void listRoots(OpsProject project, AsyncCallback<UntypedItemCollection> callback);
}
