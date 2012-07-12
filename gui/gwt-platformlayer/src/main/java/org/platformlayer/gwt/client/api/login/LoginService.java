package org.platformlayer.gwt.client.api.login;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface LoginService {
	void login(String username, String password, AsyncCallback<AuthenticateResponse> callback);
}
