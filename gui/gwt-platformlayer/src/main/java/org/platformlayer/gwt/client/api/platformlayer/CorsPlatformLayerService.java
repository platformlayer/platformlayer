package org.platformlayer.gwt.client.api.platformlayer;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.platformlayer.gwt.client.api.login.Access;
import org.platformlayer.gwt.client.api.login.Authentication;
import org.platformlayer.gwt.client.api.login.Token;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class CorsPlatformLayerService implements PlatformLayerService {
	static final Logger log = Logger.getLogger(CorsPlatformLayerService.class.getName());

	@Override
	public void listRoots(Authentication auth, final AsyncCallback<UntypedItemCollection> callback) {
		auth.getAccess(new AsyncCallback<Access>() {

			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}

			@Override
			public void onSuccess(Access access) {
				String tokenId = null;
				List<String> projects = null;
				if (access != null) {
					Token token = access.getToken();
					if (token != null) {
						tokenId = token.getId();
					}
					projects = access.getProjects();

					log.info("Projects: " + Joiner.on(",").join(projects));
				}

				if (Strings.isNullOrEmpty(tokenId)) {
					callback.onFailure(null);
				} else {
					listRoots(tokenId, callback);
				}
			}
		});
	}

	private void listRoots(String tokenId, final AsyncCallback<UntypedItemCollection> callback) {
		String baseUrl = "https://ops.platformlayer.net:8082/v0/fathomdb.com/";

		String url = baseUrl + "roots";

		log.log(Level.INFO, "Making CORS request: GET " + url);

		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
		builder.setHeader("X-Auth-Token", tokenId);
		builder.setHeader("Accept", "application/json");
		String requestData = null;
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
					if (json == null) {
						callback.onSuccess(null);
						return;
					}

					try {
						UntypedItemCollection items = JsonUtils.safeEval(json);
						callback.onSuccess(items);
					} catch (Exception e) {
						callback.onFailure(e);
					}
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
