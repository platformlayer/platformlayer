package org.platformlayer.service.jetty.model;

import com.google.gwt.core.client.JavaScriptObject;

public class JettyContext extends com.google.gwt.core.client.JavaScriptObject {
	protected JettyContext() {
	}

	// TODO: JSNI cannot map 'Links links'

    
    public final String getId() {
	return com.gwtreboot.client.JsHelpers.getString0(this, "id");
}

	
    public final void setId(String v) {
	com.gwtreboot.client.JsHelpers.set0(this, "id", v);
}


    
    public final String getSource() {
	return com.gwtreboot.client.JsHelpers.getString0(this, "source");
}

	
    public final void setSource(String v) {
	com.gwtreboot.client.JsHelpers.set0(this, "source", v);
}



	public static final JettyContext create() {
		return JettyContext.createObject().cast();
	}
}
