package org.platformlayer.service.jetty.client.dnsrecordlist;

import org.platformlayer.gwt.client.places.ShellPlace;
import org.platformlayer.service.jetty.client.JettyPlace;
import org.platformlayer.service.jetty.client.home.HomePlace;
import org.platformlayer.service.jetty.client.jettyservice.JettyServicePlace;

public class JettyServiceListPlace extends JettyPlace {
	public static final String KEY = "services";

	public JettyServiceListPlace(HomePlace parent) {
		super(parent, KEY);
	}

	@Override
	public String getLabel() {
		return "Jetty Services";
	}

	@Override
	public ShellPlace getChild(String pathToken) {
		return getServicePlace(pathToken);
	}

	public JettyServicePlace getServicePlace(String domainKey) {
		return new JettyServicePlace(this, domainKey);
	}
}
