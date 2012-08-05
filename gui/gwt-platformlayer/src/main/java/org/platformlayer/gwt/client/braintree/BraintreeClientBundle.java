package org.platformlayer.gwt.client.braintree;

import com.fathomdb.gwt.utils.resources.JavascriptResource;
import com.google.gwt.resources.client.ClientBundle;

public interface BraintreeClientBundle extends ClientBundle {
	@Source("braintree-1.1.0.min.js")
	@JavascriptResource.Minification(minify = false)
	JavascriptResource braintreeJavascript();
}
