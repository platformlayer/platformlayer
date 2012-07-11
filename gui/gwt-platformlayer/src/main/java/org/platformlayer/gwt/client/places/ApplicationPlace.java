package org.platformlayer.gwt.client.places;

import com.google.gwt.place.shared.Place;

public abstract class ApplicationPlace extends Place {
	public abstract ApplicationPlace getParent();

	public abstract String getLabel();
}