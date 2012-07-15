package org.platformlayer.gwt.client.project;

import org.platformlayer.gwt.client.view.ApplicationView;

import com.google.inject.ImplementedBy;

@ImplementedBy(ProjectViewImpl.class)
public interface ProjectView extends ApplicationView {
	void start(ProjectActivity activity);
}
