package org.platformlayer.service.jetty.client.model;

import com.google.gwt.core.client.JavaScriptObject;

public class JettyService extends org.platformlayer.core.model.ItemBaseJs {
	protected JettyService() {
	}

	// TODO: JSNI cannot map 'List contexts'
	// TODO: JSNI cannot map 'Transport transport'

	public final native java.lang.String getDnsName()
	/*-{ return this.dnsName; }-*/;

	public final native void setDnsName(java.lang.String newValue)
	/*-{ this.dnsName = newValue; }-*/;

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
