package org.platformlayer.service.jetty.client;

import org.platformlayer.gwt.client.places.ShellPlace;
import org.platformlayer.ui.shared.client.plugin.PlugInProvider;

public abstract class JettyPlace extends ShellPlace {

	public JettyPlace(ShellPlace parent, String pathToken) {
		super(parent, pathToken);
	}

	@Override
	public PlugInProvider getPluginProvider() {
		return JettyPlugin.get();
	}
}