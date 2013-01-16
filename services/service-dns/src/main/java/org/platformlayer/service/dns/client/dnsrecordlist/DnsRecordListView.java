package org.platformlayer.service.dns.client.dnsrecordlist;

import org.platformlayer.dns.model.DnsRecord;
import org.platformlayer.gwt.client.ui.ListView;

import com.google.inject.ImplementedBy;

@ImplementedBy(DnsRecordListViewImpl.class)
public interface DnsRecordListView extends ListView<DnsRecord> {
}
