package org.platformlayer.gwt.client.api;

import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.platformlayer.gwt.client.HttpStatusCodeException;

import com.google.common.collect.Maps;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class CorsRequest {
	static final Logger log = Logger.getLogger(CorsRequest.class.getName());

	final Method method;
	String url;

	final String postData;

	protected final Map<String, String> headers = Maps.newHashMap();

	protected CorsRequest(String url, Method method, String postData) {
		super();
		this.url = url;
		this.method = method;
		this.postData = postData;
	}

	public static CorsRequest get(String url) {
		return new CorsRequest(url, RequestBuilder.GET, null);
	}

	public static CorsRequest post(String url, String postData) {
		return new CorsRequest(url, RequestBuilder.POST, postData);
	}

	public <T extends JavaScriptObject> void execute(final AsyncCallback<T> callback) {
		makeCorsRequest(callback);
	}

	protected <T extends JavaScriptObject> void makeCorsRequest(final AsyncCallback<T> callback) {
		log.log(Level.INFO, "Making CORS request: " + method + " " + url);

		RequestBuilder builder = new RequestBuilder(method, url);
		builder.setHeader("Accept", "application/json");

		for (Entry<String, String> entry : headers.entrySet()) {
			builder.setHeader(entry.getKey(), entry.getValue());
		}

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

						callback.onFailure(new HttpStatusCodeException(statusCode, statusText));
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

	public void add(String key, String value) {
		if (url.indexOf('?') != -1) {
			url += '&';
		} else {
			url += '?';
		}

		url += URL.encodeQueryString(key) + "=" + URL.encodeQueryString(value);
	}

	public void add(String key, int value) {
		add(key, String.valueOf(value));
	}

	public static String toJson(JavaScriptObject postData) {
		String json = new JSONObject(postData).toString();
		return json;
	}

	public void addHeader(String key, String value) {
		headers.put(key, value);
	}
}
