package org.platformlayer.service.dns.model;

import com.google.gwt.core.client.JavaScriptObject;

public class DnsRecord extends org.platformlayer.core.model.ItemBaseJs {
	protected DnsRecord() {
	}

	// TODO: JSNI cannot map 'long version'
	// TODO: JSNI cannot map 'ManagedItemState state'
	// TODO: JSNI cannot map 'SecretInfo secret'

    
	public final native java.lang.String getDnsName()
	/*-{ return this.dnsName; }-*/;
	
	public final native void setDnsName(java.lang.String newValue)
	/*-{ this.dnsName = newValue; }-*/;

    
	public final native java.lang.String getRecordType()
	/*-{ return this.recordType; }-*/;
	
	public final native void setRecordType(java.lang.String newValue)
	/*-{ this.recordType = newValue; }-*/;

    
    public final java.util.List<java.lang.String> getAddress() {
	com.google.gwt.core.client.JsArrayString array0 = org.platformlayer.core.model.JsHelpers.getObject0(this, "address").cast();
	return org.platformlayer.core.model.JsStringArrayToList.wrap(array0);
}

	
    


	public static final DnsRecord create() {
		return DnsRecord.createObject().cast();
	}
}
