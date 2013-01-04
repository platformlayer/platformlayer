package org.platformlayer.service.dns.model;

import com.google.gwt.core.client.JavaScriptObject;

public class DnsRecord extends org.platformlayer.core.model.ItemBaseJs {
	protected DnsRecord() {
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


    
    public final String getRecordType() {
	return org.platformlayer.core.model.JsHelpers.getString0(this, "recordType");
}

	
    public final void setRecordType(String v) {
	org.platformlayer.core.model.JsHelpers.set0(this, "recordType", v);
}


    
    public final java.util.List<java.lang.String> getAddress() {
	com.google.gwt.core.client.JsArrayString array0 = org.platformlayer.core.model.JsHelpers.getObject0(this, "address").cast();
	return org.platformlayer.core.model.JsStringArrayToList.wrap(array0);
}

	
    


	public static final DnsRecord create() {
		return DnsRecord.createObject().cast();
	}
}
