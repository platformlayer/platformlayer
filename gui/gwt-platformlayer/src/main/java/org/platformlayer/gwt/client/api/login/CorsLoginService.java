package org.platformlayer.gwt.client.api.login;

import java.util.logging.Logger;

import javax.inject.Inject;

import org.platformlayer.gwt.client.ApplicationState;
import org.platformlayer.gwt.client.api.CorsRequest;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class CorsLoginService implements LoginService {
	static final Logger log = Logger.getLogger(CorsLoginService.class.getName());

	@Inject
	ApplicationState app;

	@Override
	public void login(String username, String password, final AsyncCallback<AuthenticateResponse> callback) {
		String url = app.getAuthBaseUrl() + "tokens";

		PasswordCredentials passwordCredentials = JavaScriptObject.createObject().cast();
		passwordCredentials.setUsername(username);
		passwordCredentials.setPassword(password);

		Auth auth = JavaScriptObject.createObject().cast();
		auth.setPasswordCredentials(passwordCredentials);

		AuthenticateRequest request = JavaScriptObject.createObject().cast();
		request.setAuth(auth);

		CorsRequest.post(url, CorsRequest.toJson(request)).execute(callback);
	}

	@Override
	public void register(String username, String password, AsyncCallback<RegisterResponse> callback) {
		String url = app.getAuthBaseUrl() + "register";

		RegisterRequest request = JavaScriptObject.createObject().cast();
		request.setUsername(username);
		request.setPassword(password);

		CorsRequest.post(url, CorsRequest.toJson(request)).execute(callback);
	}

}
