package org.platformlayer.gwt.client.breadcrumb;

import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.base.ListItem;
import com.github.gwtbootstrap.client.ui.incubator.Breadcrumbs;
import com.google.gwt.user.client.ui.WidgetCollection;

public class BootstrapBreadcrumbs extends Breadcrumbs {

	@Override
	public void add(NavLink link) {
		WidgetCollection children = getChildren();
		int size = children.size();
		if (size != 0) {
			ListItem previous = (ListItem) children.get(size - 1);
			previous.removeStyleName("active");

			SpanElement span = new SpanElement();
			span.setStyleName("divider");
			previous.add(span);
		}

		ListItem li = new ListItem(link);
		li.addStyleName("active");
		super.add(li);
	}

}
