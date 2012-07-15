package org.platformlayer.gwt.client.places;

import org.platformlayer.gwt.client.project.ProjectPlace;

import com.google.gwt.place.shared.Place;

public abstract class ApplicationPlace extends Place {
	final ApplicationPlace parent;
	final String pathToken;

	public ApplicationPlace(ApplicationPlace parent, String pathToken) {
		this.parent = parent;
		this.pathToken = pathToken;

		assert pathToken != null;
	}

	public abstract String getLabel();

	public ApplicationPlace getParent() {
		return parent;
	}

	public String getPathToken() {
		return pathToken;
	}

	public abstract ApplicationPlace getChild(String pathToken);

	protected ProjectPlace getProject() {
		if (parent == null)
			return null;
		return parent.getProject();
	}
}