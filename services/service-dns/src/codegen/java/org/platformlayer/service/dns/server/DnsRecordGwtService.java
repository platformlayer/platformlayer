package org.platformlayer.service.dns.server;

import org.platformlayer.service.dns.model.DnsRecord;
import org.platformlayer.ui.shared.server.GwtServiceBase;

public class DnsRecordGwtService extends GwtServiceBase<DnsRecord> {
    public DnsRecordGwtService() {
        super(DnsRecord.class);
    }
}