package org.platformlayer.service.dns.client.dnsrecord;

import org.platformlayer.gwt.client.ui.ItemView;
import org.platformlayer.service.dns.model.DnsRecord;

import com.google.inject.ImplementedBy;

@ImplementedBy(DnsRecordViewImpl.class)
public interface DnsRecordView extends ItemView<DnsRecord> {
}
