package org.platformlayer.gwt.client.home;

import org.platformlayer.gwt.client.view.ApplicationView;

import com.google.inject.ImplementedBy;

@ImplementedBy(HomeViewImpl.class)
public interface HomeView extends ApplicationView {
	void start(HomeActivity homeActivity);
}
