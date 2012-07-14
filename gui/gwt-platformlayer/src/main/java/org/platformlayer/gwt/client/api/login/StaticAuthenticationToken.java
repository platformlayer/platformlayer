package org.platformlayer.gwt.client.api.login;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * An authentication token that we presume never expires
 * 
 */
public class StaticAuthenticationToken implements Authentication {

	private final Access access;

	public StaticAuthenticationToken(Access access) {
		this.access = access;
	}

	@Override
	public void getAccess(AsyncCallback<Access> asyncCallback) {
		asyncCallback.onSuccess(access);
	}

}
