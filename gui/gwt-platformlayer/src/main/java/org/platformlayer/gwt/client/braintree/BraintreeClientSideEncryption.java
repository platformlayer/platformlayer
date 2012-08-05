package org.platformlayer.gwt.client.braintree;

import com.google.gwt.core.client.JavaScriptObject;

public final class BraintreeClientSideEncryption extends JavaScriptObject {
	protected BraintreeClientSideEncryption() {
	}

	static final native BraintreeClientSideEncryption create(String clientSideEncryptionKey)
	/*-{
	return $wnd.Braintree.create(clientSideEncryptionKey);
	}-*/;

	public final native String encrypt(String plainTextValue)
	/*-{
	return this.encrypt(plainTextValue);
	}-*/;

}