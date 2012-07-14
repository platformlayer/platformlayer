package org.platformlayer.gwt.client.projectlist;

import org.platformlayer.gwt.client.view.ApplicationView;

import com.google.inject.ImplementedBy;

@ImplementedBy(ProjectListViewImpl.class)
public interface ProjectListView extends ApplicationView {
	void start(ProjectListActivity activity);
}
