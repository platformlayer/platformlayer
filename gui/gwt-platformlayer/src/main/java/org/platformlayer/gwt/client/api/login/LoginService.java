package org.platformlayer.gwt.client.api.login;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.ImplementedBy;

@ImplementedBy(CorsLoginService.class)
public interface LoginService {
	void login(String username, String password, AsyncCallback<AuthenticateResponse> callback);

	void register(String username, String password, AsyncCallback<RegisterResponse> callback);
}
