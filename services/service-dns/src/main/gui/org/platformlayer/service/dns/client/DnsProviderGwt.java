package org.platformlayer.service.dns.client;

import org.platformlayer.service.dns.shared.DnsRecordProxy;
import org.platformlayer.service.dns.shared.DnsServerProxy;
import org.platformlayer.service.dns.shared.DnsZoneProxy;

public interface DnsProviderGwt {
    public static interface ProviderRequestFactory extends DnsRecordProxy.DnsRecordRequestFactory, DnsServerProxy.DnsServerRequestFactory, DnsZoneProxy.DnsZoneRequestFactory {

    }
}
