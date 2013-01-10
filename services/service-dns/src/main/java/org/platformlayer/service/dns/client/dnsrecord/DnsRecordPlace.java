package org.platformlayer.service.dns.client.dnsrecord;

import org.platformlayer.gwt.client.places.ShellPlace;
import org.platformlayer.service.dns.client.DnsPlace;

public class DnsRecordPlace extends DnsPlace {

	public static final String LABEL = "DNS Record";

	public DnsRecordPlace(DnsPlace parent, String id) {
		super(parent, id);
	}

	@Override
	public String getLabel() {
		return LABEL;
	}

	@Override
	public ShellPlace getChild(String pathToken) {
		return null;
	}

}
