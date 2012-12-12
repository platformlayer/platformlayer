package org.platformlayer.service.jetty.client.jettyservicelist;

import org.platformlayer.common.IsItem;
import org.platformlayer.gwt.client.jobs.JobPlace;
import org.platformlayer.gwt.client.places.ShellPlace;
import org.platformlayer.gwt.client.ui.ListActivity;
import org.platformlayer.gwt.client.ui.ListView;
import org.platformlayer.service.jetty.client.JettyPlugin;
import org.platformlayer.service.jetty.model.JettyService;

import com.google.inject.Inject;

public class JettyServiceListActivity extends ListActivity<JettyServiceListPlace, ListView<JettyService>, JettyService> {
	protected JettyServiceListActivity() {
		super(JettyPlugin.SERVICE_TYPE, JettyPlugin.ITEM_TYPE_JETTYSERVICE);
	}

	@Inject
	JettyServiceListViewImpl view;

	@Override
	protected JettyServiceListViewImpl getView() {
		return view;
	}

	public void onJobClick(IsItem value, String jobId) {
		ShellPlace context = getPlace();
		ShellPlace jobPlace = JobPlace.build(context, jobId);
		goTo(jobPlace);
	}
}
