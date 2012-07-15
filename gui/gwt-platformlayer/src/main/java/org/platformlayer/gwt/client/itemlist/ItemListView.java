package org.platformlayer.gwt.client.itemlist;

import org.platformlayer.gwt.client.api.platformlayer.UntypedItem;
import org.platformlayer.gwt.client.view.ApplicationView;

import com.google.gwt.user.cellview.client.CellList;
import com.google.inject.ImplementedBy;

@ImplementedBy(ItemListViewImpl.class)
public interface ItemListView extends ApplicationView {
	void start(ItemListActivity activity);

	CellList<UntypedItem> getItemList();
}
