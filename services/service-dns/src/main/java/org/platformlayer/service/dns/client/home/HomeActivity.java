package org.platformlayer.service.dns.client.home;

import java.util.logging.Logger;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.gwt.client.ui.PluginActivity;
import org.platformlayer.service.dns.client.DnsPlugin;

import com.google.inject.Inject;

public class HomeActivity extends PluginActivity<HomePlace, HomeView> {
	static final Logger log = Logger.getLogger(HomeActivity.class.getName());

	public HomeActivity() {
		super(DnsPlugin.SERVICE_TYPE, null);
	}

	@Inject
	HomeView view;

	@Override
	protected HomeView getView() {
		return view;
	}

	@Override
	protected PlatformLayerKey getKey() {
		return buildPlatformLayerKey(serviceType, null, null);
	}

	@Override
	protected void initializeView() {
	}

}