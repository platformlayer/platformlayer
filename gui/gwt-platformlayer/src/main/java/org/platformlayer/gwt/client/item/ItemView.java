package org.platformlayer.gwt.client.item;

import org.platformlayer.gwt.client.view.ApplicationView;

import com.google.inject.ImplementedBy;

@ImplementedBy(ItemViewImpl.class)
public interface ItemView extends ApplicationView {
	void start(ItemActivity activity);
}
