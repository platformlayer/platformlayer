package org.platformlayer.service.jetty.client.home;

import org.platformlayer.gwt.client.places.ShellPlace;
import org.platformlayer.service.jetty.client.JettyPlace;
import org.platformlayer.service.jetty.client.JettyPlugin;
import org.platformlayer.service.jetty.client.dnsrecordlist.JettyServiceListPlace;

public class HomePlace extends JettyPlace {
	public HomePlace(ShellPlace parent) {
		super(parent, JettyPlugin.KEY);
	}

	@Override
	public String getLabel() {
		return "DNS";
	}

	@Override
	public ShellPlace getChild(String pathToken) {
		if (pathToken.equals(JettyServiceListPlace.KEY)) {
			return getDomainListPlace();
		}
		return null;
	}

	public JettyServiceListPlace getDomainListPlace() {
		return new JettyServiceListPlace(this);
	}
}
