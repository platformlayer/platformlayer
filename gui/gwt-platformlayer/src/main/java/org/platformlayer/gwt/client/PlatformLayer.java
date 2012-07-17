package org.platformlayer.gwt.client;

import org.platformlayer.gwt.client.bootstrap.Bootstrap;
import org.platformlayer.gwt.client.breadcrumb.HeaderActivityMapper;
import org.platformlayer.gwt.client.login.LoginPlace;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

public class PlatformLayer implements EntryPoint {
	private final ApplicationGinjector injector = GWT.create(ApplicationGinjector.class);
	private final SimplePanel appWidget = new SimplePanel();
	private final SimplePanel headerWidget = new SimplePanel();

	/**
	 * This is the entry point method.
	 */
	@Override
	public void onModuleLoad() {
		RootPanel rootPanel = RootPanel.get("mainContainer");

		Bootstrap.ensureInjected();

		RootPanel headerContainer = RootPanel.get("headerContainer");
		headerContainer.add(injector.getHeaderView());

		{
			ContentActivityMapper activityMapper = injector.getContentActivityMapper();
			ActivityManager activityManager = new ActivityManager(activityMapper, injector.getEventBus());
			activityManager.setDisplay(appWidget);
		}

		{
			HeaderActivityMapper activityMapper = injector.getBreadcrumbActivityMapper();
			ActivityManager activityManager = new ActivityManager(activityMapper, injector.getEventBus());
			activityManager.setDisplay(headerWidget);
		}

		PlaceHistoryMapper historyMapper = injector.getPlaceHistoryMapper();
		PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);

		rootPanel.add(headerWidget);
		rootPanel.add(appWidget);

		Place defaultPlace = LoginPlace.INSTANCE;
		historyHandler.register(injector.getPlaceController(), injector.getEventBus(), defaultPlace);
		historyHandler.handleCurrentHistory();
	}
}
