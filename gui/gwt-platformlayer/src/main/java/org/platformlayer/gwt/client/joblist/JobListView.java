package org.platformlayer.gwt.client.joblist;

import org.platformlayer.gwt.client.api.platformlayer.Job;
import org.platformlayer.gwt.client.view.ApplicationView;

import com.google.gwt.user.cellview.client.CellList;
import com.google.inject.ImplementedBy;

@ImplementedBy(JobListViewImpl.class)
public interface JobListView extends ApplicationView {
	void start(JobListActivity activity);

	CellList<Job> getJobList();

}
