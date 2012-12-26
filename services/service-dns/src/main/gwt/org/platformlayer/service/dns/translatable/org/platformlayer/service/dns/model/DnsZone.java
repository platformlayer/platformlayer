package org.platformlayer.service.dns.model;

import com.google.gwt.core.client.JavaScriptObject;

public class DnsZone extends org.platformlayer.core.model.ItemBaseJs {
	protected DnsZone() {
	}

	// TODO: JSNI cannot map 'long version'
	// TODO: JSNI cannot map 'ManagedItemState state'
	// TODO: JSNI cannot map 'SecretInfo secret'

    
	public final native java.lang.String getDnsName()
	/*-{ return this.dnsName; }-*/;
	
	public final native void setDnsName(java.lang.String newValue)
	/*-{ this.dnsName = newValue; }-*/;


	public static final DnsZone create() {
		return DnsZone.createObject().cast();
	}
}
