package org.platformlayer.service.jetty.client.jettyservice;

import org.platformlayer.gwt.client.places.ShellPlace;
import org.platformlayer.service.jetty.client.JettyPlace;

public class JettyServicePlace extends JettyPlace {

	public JettyServicePlace(JettyPlace parent, String id) {
		super(parent, id);
	}

	@Override
	public String getLabel() {
		return "Jetty Service";
	}

	@Override
	public ShellPlace getChild(String pathToken) {
		return null;
	}

}
