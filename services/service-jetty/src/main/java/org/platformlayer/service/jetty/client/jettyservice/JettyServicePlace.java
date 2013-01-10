package org.platformlayer.service.jetty.client.jettyservice;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.gwt.client.places.ShellPlace;
import org.platformlayer.service.jetty.client.JettyPlace;
import org.platformlayer.service.jetty.client.JettyPlugin;

public class JettyServicePlace extends JettyPlace {

	public static final String LABEL = "Jetty Service";

	public JettyServicePlace(JettyPlace parent, String id) {
		super(parent, id);
	}

	@Override
	public String getLabel() {
		return LABEL;
	}

	@Override
	public ShellPlace getChild(String pathToken) {
		return null;
	}

	@Override
	public PlatformLayerKey getPlatformLayerKey() {
		return buildPlatformLayerKey(JettyPlugin.SERVICE_TYPE, JettyPlugin.ITEM_TYPE_JETTYSERVICE, getPathToken());
	}

}
