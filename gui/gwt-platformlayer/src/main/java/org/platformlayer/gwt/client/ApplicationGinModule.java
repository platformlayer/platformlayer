package org.platformlayer.gwt.client;

import org.platformlayer.gwt.client.api.login.JsonpLoginService;
import org.platformlayer.gwt.client.api.login.LoginService;
import org.platformlayer.gwt.client.api.platformlayer.CorsPlatformLayerService;
import org.platformlayer.gwt.client.api.platformlayer.PlatformLayerService;
import org.platformlayer.gwt.client.breadcrumb.HeaderActivityMapper;
import org.platformlayer.gwt.client.breadcrumb.HeaderView;
import org.platformlayer.gwt.client.breadcrumb.HeaderViewImpl;
import org.platformlayer.gwt.client.home.HomeView;
import org.platformlayer.gwt.client.home.HomeViewImpl;
import org.platformlayer.gwt.client.login.LoginView;
import org.platformlayer.gwt.client.login.LoginViewImpl;

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

public class ApplicationGinModule extends AbstractGinModule {
	@Override
	protected void configure() {
		bind(EventBus.class).to(SimpleEventBus.class).in(Singleton.class);
		bind(PlaceController.class).to(InjectablePlaceController.class).in(Singleton.class);

		bind(ContentActivityMapper.class).in(Singleton.class);
		bind(HeaderActivityMapper.class).in(Singleton.class);

		bind(HeaderView.class).to(HeaderViewImpl.class).in(Singleton.class);

		bind(HomeView.class).to(HomeViewImpl.class).in(Singleton.class);
		bind(LoginView.class).to(LoginViewImpl.class).in(Singleton.class);

		bind(LoginService.class).to(JsonpLoginService.class).in(Singleton.class);
		bind(PlatformLayerService.class).to(CorsPlatformLayerService.class).in(Singleton.class);

		bind(PlaceHistoryMapper.class).toProvider(ApplicationPlaceHistoryMapper.Factory.class).in(Singleton.class);
	}
}