package org.platformlayer.service.dns.client.dnsrecord;

import org.platformlayer.dns.model.DnsRecord;
import org.platformlayer.gwt.client.ui.ItemView;

import com.google.inject.ImplementedBy;

@ImplementedBy(DnsRecordViewImpl.class)
public interface DnsRecordView extends ItemView<DnsRecord> {
}
