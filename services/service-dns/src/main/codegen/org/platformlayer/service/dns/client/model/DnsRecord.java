package org.platformlayer.service.dns.client.model;

import com.google.gwt.core.client.JavaScriptObject;

public class DnsRecord extends JavaScriptObject {
	protected DnsRecord() {
	}

	// TODO: JSNI cannot map 'List address'
	// TODO: JSNI cannot map 'PlatformLayerKey key'
	// TODO: JSNI cannot map 'long version'
	// TODO: JSNI cannot map 'ManagedItemState state'
	// TODO: JSNI cannot map 'Tags tags'
	// TODO: JSNI cannot map 'SecretInfo secret'
    
	public final native java.lang.String getDnsName()
	/*-{ return this.dnsName; }-*/;

	public final native void setDnsName(java.lang.String newValue)
	/*-{ this.dnsName = newValue; }-*/;
	public final native java.lang.String getRecordType()
	/*-{ return this.recordType; }-*/;

	public final native void setRecordType(java.lang.String newValue)
	/*-{ this.recordType = newValue; }-*/;
}
