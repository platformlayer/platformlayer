package org.platformlayer.service.jetty.model;

import com.google.gwt.core.client.JavaScriptObject;

public class JettyService extends org.platformlayer.core.model.ItemBaseJs {
	protected JettyService() {
	}

	// TODO: JSNI cannot map 'Transport transport'
	// TODO: JSNI cannot map 'long version'
	// TODO: JSNI cannot map 'ManagedItemState state'
	// TODO: JSNI cannot map 'SecretInfo secret'

    
	public final native java.lang.String getDnsName()
	/*-{ return this.dnsName; }-*/;
	
	public final native void setDnsName(java.lang.String newValue)
	/*-{ this.dnsName = newValue; }-*/;

    
    public final java.util.List<org.platformlayer.service.jetty.model.JettyContext> getContexts() {
	com.google.gwt.core.client.JsArray<org.platformlayer.service.jetty.model.JettyContext> array0 = org.platformlayer.core.model.JsHelpers.getObject0(this, "contexts").cast();
	return org.platformlayer.core.model.JsArrayToList.wrap(array0);
}

	
    

    
    public final org.platformlayer.core.model.PlatformLayerKey getSslKey() {
		return org.platformlayer.core.model.PlatformLayerKeyJs.get(this, "sslKey");
	}
	
	public final void setSslKey(org.platformlayer.core.model.PlatformLayerKey newValue) {
		org.platformlayer.core.model.PlatformLayerKeyJs.set(this, "sslKey", newValue);
	}


	public static final JettyService create() {
		return JettyService.createObject().cast();
	}
}
