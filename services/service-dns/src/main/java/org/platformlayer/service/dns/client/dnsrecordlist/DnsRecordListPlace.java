package org.platformlayer.service.dns.client.dnsrecordlist;

import org.platformlayer.gwt.client.places.ShellPlace;
import org.platformlayer.service.dns.client.DnsPlace;
import org.platformlayer.service.dns.client.dnsrecord.DnsRecordPlace;
import org.platformlayer.service.dns.client.home.HomePlace;

public class DnsRecordListPlace extends DnsPlace {
	public static final String KEY = "records";

	public DnsRecordListPlace(HomePlace parent) {
		super(parent, KEY);
	}

	@Override
	public String getLabel() {
		return "Records";
	}

	@Override
	public ShellPlace getChild(String pathToken) {
		return getDomainPlace(pathToken);
	}

	public DnsRecordPlace getDomainPlace(String domainKey) {
		return new DnsRecordPlace(this, domainKey);
	}
}
