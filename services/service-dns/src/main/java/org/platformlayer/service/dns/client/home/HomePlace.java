package org.platformlayer.service.dns.client.home;

import org.platformlayer.gwt.client.places.ShellPlace;
import org.platformlayer.service.dns.client.DnsPlace;
import org.platformlayer.service.dns.client.DnsPlugin;
import org.platformlayer.service.dns.client.dnsrecordlist.DnsRecordListPlace;

public class HomePlace extends DnsPlace {
	public HomePlace(ShellPlace parent) {
		super(parent, DnsPlugin.KEY);
	}

	@Override
	public String getLabel() {
		return "DNS";
	}

	@Override
	public ShellPlace getChild(String pathToken) {
		if (pathToken.equals(DnsRecordListPlace.KEY)) {
			return getDomainListPlace();
		}
		return null;
	}

	public DnsRecordListPlace getDomainListPlace() {
		return new DnsRecordListPlace(this);
	}
}
