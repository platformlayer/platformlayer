package org.platformlayer.service.dns.client.dnsrecord;

import java.util.logging.Logger;

import org.platformlayer.gwt.client.ui.ItemActivity;
import org.platformlayer.service.dns.client.DnsPlugin;
import org.platformlayer.service.dns.client.model.DnsRecord;

import com.google.inject.Inject;

public class DnsRecordActivity extends ItemActivity<DnsRecordPlace, DnsRecordView, DnsRecord> {
	static final Logger log = Logger.getLogger(DnsRecordActivity.class.getName());

	public DnsRecordActivity() {
		super(DnsPlugin.SERVICE_TYPE, DnsPlugin.ITEM_TYPE_DNSRECORD);
	}

	@Inject
	DnsRecordView view;

	@Override
	protected DnsRecordView getView() {
		return view;
	}
}