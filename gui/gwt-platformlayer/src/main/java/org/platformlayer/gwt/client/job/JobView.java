package org.platformlayer.gwt.client.job;

import org.platformlayer.gwt.client.view.ApplicationView;

import com.google.inject.ImplementedBy;

@ImplementedBy(JobViewImpl.class)
public interface JobView extends ApplicationView {
	void start(JobActivity activity);
}
