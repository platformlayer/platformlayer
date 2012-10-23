package org.platformlayer.service.dns.client.model;

import com.google.gwt.core.client.JavaScriptObject;

public class DnsZone extends JavaScriptObject {
	protected DnsZone() {
	}

	// TODO: JSNI cannot map 'long version'
	// TODO: JSNI cannot map 'ManagedItemState state'
	// TODO: JSNI cannot map 'Tags tags'
	// TODO: JSNI cannot map 'SecretInfo secret'
    
	public final native java.lang.String getDnsName()
	/*-{ return this.dnsName; }-*/;

	public final native void setDnsName(java.lang.String newValue)
	/*-{ this.dnsName = newValue; }-*/;
	public final native org.platformlayer.gwt.client.api.platformlayer.PlatformLayerKeyJs getKey()
	/*-{ return this.key; }-*/;

	public final native void setKey(org.platformlayer.gwt.client.api.platformlayer.PlatformLayerKeyJs newValue)
	/*-{ this.key = newValue; }-*/;
}
