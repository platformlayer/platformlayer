package org.platformlayer.gwt.client.api.platformlayer;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.platformlayer.gwt.client.api.login.Access;
import org.platformlayer.gwt.client.api.login.Authentication;
import org.platformlayer.gwt.client.api.login.Token;

import com.google.common.base.Strings;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class CorsRequest<T extends JavaScriptObject> {
	static final Logger log = Logger.getLogger(CorsRequest.class.getName());

	final OpsProject project;
	final String url;

	final String postData;

	public CorsRequest(OpsProject project, String url, String postData) {
		super();
		this.url = url;
		this.project = project;
		this.postData = postData;
	}

	public CorsRequest(OpsProject project, String url) {
		this(project, url, null);
	}

	public void execute(final AsyncCallback<T> callback) {
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
					execute(project, tokenId, callback);
				}
			}
		});
	}

	private void execute(OpsProject project, String tokenId, final AsyncCallback<T> callback) {
		log.log(Level.INFO, "Making CORS request: GET " + url);

		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
		builder.setHeader("X-Auth-Token", tokenId);
		builder.setHeader("Accept", "application/json");

		if (postData != null) {
			builder.setHeader("Content-Type", "application/json");
		}

		String requestData = postData;
		try {
			builder.sendRequest(requestData, new RequestCallback() {
				@Override
				public void onResponseReceived(Request request, Response response) {
					if (response == null) {
						log.log(Level.FINE, "Response was null");

						callback.onFailure(null);
						return;
					}

					int statusCode = response.getStatusCode();
					if (statusCode != 200) {
						String statusText = response.getStatusText();

						log.log(Level.FINE, "Unexpected status code: " + statusCode + " statusText=" + statusText);

						// TODO: Pass status code
						callback.onFailure(null);
						return;
					}

					String json = response.getText();
					log.log(Level.FINE, "Got response: " + json);

					if (json == null) {
						callback.onSuccess(null);
						return;
					}

					T items;
					try {
						items = JsonUtils.safeEval(json);
					} catch (Exception e) {
						callback.onFailure(e);
						return;
					}

					// TODO: Catch exceptions??
					callback.onSuccess(items);
				}

				@Override
				public void onError(Request request, Throwable exception) {
					log.log(Level.FINE, "HTTP call failed", exception);

					callback.onFailure(exception);
				}
			});
		} catch (Exception e) {
			callback.onFailure(e);
		}
	}
}
