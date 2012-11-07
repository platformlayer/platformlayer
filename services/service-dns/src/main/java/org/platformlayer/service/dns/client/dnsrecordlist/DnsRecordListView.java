package org.platformlayer.service.dns.client.dnsrecordlist;

import org.platformlayer.gwt.client.ui.ListView;
import org.platformlayer.service.dns.client.model.DnsRecord;

import com.google.inject.ImplementedBy;

@ImplementedBy(DnsRecordListViewImpl.class)
public interface DnsRecordListView extends ListView<DnsRecord> {
}
