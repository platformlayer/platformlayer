package org.platformlayer.service.jetty.model;

import com.google.gwt.core.client.JavaScriptObject;

public class JettyContext extends com.google.gwt.core.client.JavaScriptObject {
	protected JettyContext() {
	}


    
	public final native java.lang.String getId()
	/*-{ return this.id; }-*/;
	
	public final native void setId(java.lang.String newValue)
	/*-{ this.id = newValue; }-*/;

    
	public final native java.lang.String getSource()
	/*-{ return this.source; }-*/;
	
	public final native void setSource(java.lang.String newValue)
	/*-{ this.source = newValue; }-*/;


	public static final JettyContext create() {
		return JettyContext.createObject().cast();
	}
}
