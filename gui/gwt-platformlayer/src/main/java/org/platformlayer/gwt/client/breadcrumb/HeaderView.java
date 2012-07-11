package org.platformlayer.gwt.client.breadcrumb;

import java.util.List;

import org.platformlayer.gwt.client.places.ApplicationPlace;

import com.google.gwt.user.client.ui.IsWidget;

public interface HeaderView extends IsWidget {
	void setBreadcrumbs(List<ApplicationPlace> breadcrumbs);
}