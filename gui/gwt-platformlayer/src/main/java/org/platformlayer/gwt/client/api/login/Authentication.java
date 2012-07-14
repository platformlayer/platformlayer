package org.platformlayer.gwt.client.api.login;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface Authentication {
	void getAccess(AsyncCallback<Access> asyncCallback);
}
