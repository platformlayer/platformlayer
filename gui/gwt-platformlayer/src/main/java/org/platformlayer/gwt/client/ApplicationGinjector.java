package org.platformlayer.gwt.client;

import org.platformlayer.gwt.client.breadcrumb.HeaderActivityMapper;
import org.platformlayer.gwt.client.breadcrumb.HeaderView;

import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;

@GinModules(ApplicationGinModule.class)
public interface ApplicationGinjector extends Ginjector {
	EventBus getEventBus();

	PlaceController getPlaceController();

	ContentActivityMapper getContentActivityMapper();

	HeaderActivityMapper getBreadcrumbActivityMapper();

	HeaderView getHeaderView();
}
