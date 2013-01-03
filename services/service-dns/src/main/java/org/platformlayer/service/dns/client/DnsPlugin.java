package org.platformlayer.service.dns.client;

import javax.inject.Provider;
import javax.inject.Singleton;

import org.platformlayer.gwt.client.ApplicationState;
import org.platformlayer.gwt.client.ExternalPlugin;
import org.platformlayer.gwt.client.places.ShellPlace;
import org.platformlayer.service.dns.client.dnsrecord.DnsRecordActivity;
import org.platformlayer.service.dns.client.dnsrecord.DnsRecordPlace;
import org.platformlayer.service.dns.client.dnsrecordlist.DnsRecordListActivity;
import org.platformlayer.service.dns.client.dnsrecordlist.DnsRecordListPlace;
import org.platformlayer.service.dns.client.home.HomeActivity;
import org.platformlayer.service.dns.client.home.HomePlace;
import org.platformlayer.service.dns.client.splash.SplashPanel;
import org.platformlayer.ui.shared.client.plugin.PlugInProvider;
import org.platformlayer.ui.shared.client.plugin.PlugInView;
import org.platformlayer.ui.shared.client.plugin.PluginItem;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.place.shared.Place;
import com.google.inject.Inject;

@Singleton
public class DnsPlugin extends ExternalPlugin {
	public static final String KEY = "dns";

	public static final String SERVICE_TYPE = "dns";
	public static final String ITEM_TYPE_DNSRECORD = "dnsRecord";

	public DnsPlugin() {
		super(KEY, KEY);

		addItem(new PluginItem(ITEM_TYPE_DNSRECORD, "Record", DnsRecordListPlace.KEY));
	}

	@Inject
	SplashPanel homeButton;

	@Inject
	ApplicationState app;

	static DnsPlugin INSTANCE;

	@Override
	public void register() {
		if (INSTANCE != null) {
			throw new IllegalStateException();
		}
		INSTANCE = this;

		super.register();
	}

	@Override
	public PlugInView getWidget(String specifier) {
		if (specifier.equals(PlugInProvider.HOME)) {
			return homeButton;
		}

		return null;
	}

	@Override
	public HomePlace getRootPlace(Place parent) {
		HomePlace root = new HomePlace((ShellPlace) parent);
		return root;
	}

	public static DnsPlugin get() {
		return INSTANCE;
	}

	@Inject
	Provider<DnsRecordListActivity> dnsRecordListActivity;

	@Inject
	Provider<DnsRecordActivity> dnsRecordActivity;

	@Inject
	Provider<HomeActivity> homeActivity;

	@Override
	public Activity getActivity(Place place, String context) {
		if (place instanceof DnsRecordListPlace) {
			return dnsRecordListActivity.get();
		}
		if (place instanceof HomePlace) {
			return homeActivity.get();
		}
		if (place instanceof DnsRecordPlace) {
			return dnsRecordActivity.get();
		}
		return null;
	}

}
