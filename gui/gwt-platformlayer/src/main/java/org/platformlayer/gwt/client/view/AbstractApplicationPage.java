package org.platformlayer.gwt.client.view;

import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

public abstract class AbstractApplicationPage extends Composite implements ApplicationView {
	@Override
	public Widget asWidget() {
		return this;
	}

	protected static <T> void addDataDisplay(CellList<T> list, ListDataProvider<T> provider) {
		if (!provider.getDataDisplays().contains(list)) {
			provider.addDataDisplay(list);
		}
	}

}