package org.platformlayer.service.dns.client;

import org.platformlayer.gwt.client.places.ShellPlace;
import org.platformlayer.ui.shared.client.plugin.PlugInProvider;

public abstract class DnsPlace extends ShellPlace {

	public DnsPlace(ShellPlace parent, String pathToken) {
		super(parent, pathToken);
	}

	@Override
	public PlugInProvider getPluginProvider() {
		return DnsPlugin.get();
	}
}