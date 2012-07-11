package org.platformlayer.gwt.client.view;


import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public abstract class AbstractApplicationPage extends Composite implements ApplicationView {
	@Override
	public Widget asWidget() {
		return this;
	}

}