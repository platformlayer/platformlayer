package org.platformlayer.gwt.client.api.platformlayer;

import java.util.logging.Logger;

import org.platformlayer.gwt.client.api.CorsRequest;
import org.platformlayer.gwt.client.api.login.Access;
import org.platformlayer.gwt.client.api.login.Authentication;
import org.platformlayer.gwt.client.api.login.Token;

import com.google.common.base.Strings;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class AuthenticatedCorsRequest extends CorsRequest {
	static final Logger log = Logger.getLogger(AuthenticatedCorsRequest.class.getName());

	final OpsProject project;

	private AuthenticatedCorsRequest(String url, Method method, String postData, OpsProject project) {
		super(url, method, postData);
		this.project = project;
	}

	public static AuthenticatedCorsRequest get(OpsProject project, String url) {
		return new AuthenticatedCorsRequest(url, RequestBuilder.GET, null, project);
	}

	public static AuthenticatedCorsRequest post(OpsProject project, String url, String postData) {
		return new AuthenticatedCorsRequest(url, RequestBuilder.POST, postData, project);
	}

	@Override
	public <T extends JavaScriptObject> void execute(final AsyncCallback<T> callback) {
		Authentication auth = project.getAuthentication();

		auth.getAccess(new AsyncCallback<Access>() {

			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}

			@Override
			public void onSuccess(Access access) {
				String tokenId = null;
				if (access != null) {
					Token token = access.getToken();
					if (token != null) {
						tokenId = token.getId();
					}
				}

				if (Strings.isNullOrEmpty(tokenId)) {
					callback.onFailure(null);
				} else {
					headers.put("X-Auth-Token", tokenId);

					AuthenticatedCorsRequest.this.makeCorsRequest(callback);
				}
			}
		});
	}

}
