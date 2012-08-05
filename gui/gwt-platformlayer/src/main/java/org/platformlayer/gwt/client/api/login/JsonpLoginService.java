//package org.platformlayer.gwt.client.api.login;
//
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//import javax.inject.Inject;
//
//import org.platformlayer.gwt.client.ApplicationState;
//
//import com.google.gwt.http.client.URL;
//import com.google.gwt.jsonp.client.JsonpRequestBuilder;
//import com.google.gwt.user.client.rpc.AsyncCallback;
//
//public class JsonpLoginService implements LoginService {
//	static final Logger log = Logger.getLogger(JsonpLoginService.class.getName());
//
//	@Inject
//	ApplicationState app;
//
//	@Override
//	public void login(String username, String password, final AsyncCallback<AuthenticateResponse> callback) {
//		String tokensUrl = app.getAuthBaseUrl() + "tokens";
//
//		String url = tokensUrl + "?user=" + URL.encodeQueryString(username);
//		url += "&password=" + URL.encodeQueryString(password);
//
//		log.log(Level.INFO, "Making JSONP request to: " + url);
//
//		JsonpRequestBuilder builder = new JsonpRequestBuilder();
//		builder.requestObject(url, new AsyncCallback<AuthenticateResponse>() {
//			@Override
//			public void onFailure(Throwable e) {
//				log.log(Level.FINE, "JSONP call failed", e);
//
//				callback.onFailure(e);
//			}
//
//			@Override
//			public void onSuccess(AuthenticateResponse result) {
//				callback.onSuccess(result);
//			}
//		});
//	}
// }
