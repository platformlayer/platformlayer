package org.platformlayer.gwt.client;

import org.platformlayer.gwt.client.breadcrumb.HeaderActivity;
import org.platformlayer.gwt.client.breadcrumb.HeaderActivityMapper;
import org.platformlayer.gwt.client.breadcrumb.HeaderView;
import org.platformlayer.gwt.client.home.HomeActivity;
import org.platformlayer.gwt.client.itemlist.ItemListActivity;
import org.platformlayer.gwt.client.login.LoginActivity;
import org.platformlayer.gwt.client.projectlist.ProjectListActivity;

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

	LoginActivity getLoginActivity();

	HomeActivity getHomeActivity();

	HeaderActivity getHeaderActivity();

	ProjectListActivity getProjectListActivity();

	ItemListActivity getItemListActivity();
}
