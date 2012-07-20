package org.platformlayer.gwt.client.bootstrap;

import com.fathomdb.gwt.utils.resources.SimpleCssResource;
import com.google.gwt.resources.client.ClientBundle;

public interface BootstrapResources extends ClientBundle {

	// Only for runtime use => don't package
	// @Source("less-1.3.0.min.js")
	// JavascriptResource lessJs();

	@Source("org/platformlayer/gwt/public/bootstrap/docs/assets/css/bootstrap.css")
	SimpleCssResource bootstrapCss();
}
