package org.platformlayer.service.dns.server;

import org.platformlayer.service.dns.model.DnsServer;
import org.platformlayer.ui.shared.server.GwtServiceBase;

public class DnsServerGwtService extends GwtServiceBase<DnsServer> {
    public DnsServerGwtService() {
        super(DnsServer.class);
    }
}