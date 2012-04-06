package org.platformlayer.service.dns.server;

import org.platformlayer.service.dns.model.DnsZone;
import org.platformlayer.ui.shared.server.GwtServiceBase;

public class DnsZoneGwtService extends GwtServiceBase<DnsZone> {
    public DnsZoneGwtService() {
        super(DnsZone.class);
    }
}