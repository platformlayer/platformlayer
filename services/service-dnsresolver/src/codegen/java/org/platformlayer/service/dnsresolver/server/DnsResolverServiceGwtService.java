package org.platformlayer.service.dnsresolver.server;

import org.platformlayer.service.dnsresolver.model.DnsResolverService;
import org.platformlayer.ui.shared.server.GwtServiceBase;

public class DnsResolverServiceGwtService extends GwtServiceBase<DnsResolverService> {
    public DnsResolverServiceGwtService() {
        super(DnsResolverService.class);
    }
}