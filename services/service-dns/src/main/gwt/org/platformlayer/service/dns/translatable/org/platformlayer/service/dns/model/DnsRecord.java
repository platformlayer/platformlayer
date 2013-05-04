package org.platformlayer.service.dns.model;

import com.google.gwt.core.client.JavaScriptObject;

public class DnsRecord extends org.platformlayer.core.model.ItemBaseJs {
	protected DnsRecord() {
	}

	// TODO: JSNI cannot map 'long version'
	// TODO: JSNI cannot map 'ManagedItemState state'
	// TODO: JSNI cannot map 'Links links'
	// TODO: JSNI cannot map 'SecretInfo secret'

    
    public final String getDnsName() {
	return com.gwtreboot.client.JsHelpers.getString0(this, "dnsName");
}

	
    public final void setDnsName(String v) {
	com.gwtreboot.client.JsHelpers.set0(this, "dnsName", v);
}


    
    public final String getRecordType() {
	return com.gwtreboot.client.JsHelpers.getString0(this, "recordType");
}

	
    public final void setRecordType(String v) {
	com.gwtreboot.client.JsHelpers.set0(this, "recordType", v);
}


    
    public final java.util.List<java.lang.String> getAddress() {
	com.google.gwt.core.client.JsArrayString array0 = com.gwtreboot.client.JsHelpers.getObject0(this, "address").cast();
	return org.platformlayer.core.model.JsStringArrayToList.wrap(array0);
}

	
    


	public static final DnsRecord create() {
		return DnsRecord.createObject().cast();
	}
}
