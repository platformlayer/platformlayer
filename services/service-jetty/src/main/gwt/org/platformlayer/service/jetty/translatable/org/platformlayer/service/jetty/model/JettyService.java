package org.platformlayer.service.jetty.model;

import com.google.gwt.core.client.JavaScriptObject;

public class JettyService extends org.platformlayer.core.model.ItemBaseJs {
	protected JettyService() {
	}

	// TODO: JSNI cannot map 'Transport transport'
	// TODO: JSNI cannot map 'long version'
	// TODO: JSNI cannot map 'ManagedItemState state'
	// TODO: JSNI cannot map 'SecretInfo secret'

    
    public final String getDnsName() {
	return com.gwtreboot.client.JsHelpers.getString0(this, "dnsName");
}

	
    public final void setDnsName(String v) {
	com.gwtreboot.client.JsHelpers.set0(this, "dnsName", v);
}


    
    public final java.util.List<org.platformlayer.service.jetty.model.JettyContext> getContexts() {
	com.google.gwt.core.client.JsArray<org.platformlayer.service.jetty.model.JettyContext> array0 = com.gwtreboot.client.JsHelpers.getObject0(this, "contexts").cast();
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
