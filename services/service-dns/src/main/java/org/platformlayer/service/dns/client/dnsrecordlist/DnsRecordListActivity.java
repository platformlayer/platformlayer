package org.platformlayer.service.dns.client.dnsrecordlist;

import java.util.logging.Logger;

import org.platformlayer.common.IsItem;
import org.platformlayer.gwt.client.jobs.JobPlace;
import org.platformlayer.gwt.client.places.ShellPlace;
import org.platformlayer.gwt.client.ui.ListActivity;
import org.platformlayer.service.dns.client.DnsPlugin;
import org.platformlayer.service.dns.client.dnsrecord.DnsRecordPlace;
import org.platformlayer.service.dns.model.DnsRecord;

import com.google.inject.Inject;

public class DnsRecordListActivity extends ListActivity<DnsRecordPlace, DnsRecordListView, DnsRecord> {
	protected DnsRecordListActivity() {
		super(DnsPlugin.SERVICE_TYPE, DnsPlugin.ITEM_TYPE_DNSRECORD);
	}

	static final Logger log = Logger.getLogger(DnsRecordListActivity.class.getName());

	@Inject
	DnsRecordListView view;

	@Override
	protected DnsRecordListView getView() {
		return view;
	}

	public void onJobClick(IsItem value, String jobId) {
		ShellPlace context = getPlace();
		ShellPlace jobPlace = JobPlace.build(context, jobId);
		goTo(jobPlace);
	}
}