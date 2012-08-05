package org.platformlayer.gwt.client.braintree;

import com.google.gwt.core.client.GWT;

public class BraintreeJavascript {
	private static boolean injected = false;
	private static String braintreePublicKey;

	public static void ensureInjected() {
		if (!injected) {
			BraintreeClientBundle bundle = GWT.create(BraintreeClientBundle.class);

			bundle.braintreeJavascript().ensureInjected();
			injected = true;
		}
	}

	public static BraintreeClientSideEncryption buildEncryptor() {
		if (braintreePublicKey == null) {
			BraintreeConstants constants = GWT.create(BraintreeConstants.class);
			braintreePublicKey = constants.braintreePublicKey();
			assert braintreePublicKey != null;
		}
		return buildEncryptor(braintreePublicKey);
	}

	public static BraintreeClientSideEncryption buildEncryptor(String braintreePublicKey) {
		ensureInjected();
		return BraintreeClientSideEncryption.create(braintreePublicKey);
	}

}
