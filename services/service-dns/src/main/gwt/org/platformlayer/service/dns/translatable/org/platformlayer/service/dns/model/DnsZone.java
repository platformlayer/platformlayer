package org.platformlayer.service.dns.model;

import com.google.gwt.core.client.JavaScriptObject;

public class DnsZone extends org.platformlayer.core.model.ItemBaseJs {
	protected DnsZone() {
	}

	// TODO: JSNI cannot map 'long version'
	// TODO: JSNI cannot map 'ManagedItemState state'
	// TODO: JSNI cannot map 'SecretInfo secret'

    
    public final String getDnsName() {
	return org.platformlayer.core.model.JsHelpers.getString0(this, "dnsName");
}

	
    public final void setDnsName(String v) {
	org.platformlayer.core.model.JsHelpers.set0(this, "dnsName", v);
}



	public static final DnsZone create() {
		return DnsZone.createObject().cast();
	}
}
