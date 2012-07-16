package org.platformlayer.gwt.client.itemlist;

import javax.inject.Inject;

import org.platformlayer.gwt.client.ApplicationState;
import org.platformlayer.gwt.client.api.platformlayer.UntypedItem;
import org.platformlayer.gwt.client.view.AbstractApplicationPage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.HTMLPanel;

public class ItemListViewImpl extends AbstractApplicationPage implements ItemListView {
	interface ViewUiBinder extends UiBinder<HTMLPanel, ItemListViewImpl> {
	}

	@Inject
	ApplicationState app;

	@UiField
	CellList<UntypedItem> itemList;

	@UiFactory
	CellList<UntypedItem> makeItemList() {
		CellList<UntypedItem> table = new CellList<UntypedItem>(new ItemListCell(this));

		table.setPageSize(1000);

		// final SingleSelectionModel<OpsProject> selectionModel = new SingleSelectionModel<Product>();
		// table.setSelectionModel(selectionModel);
		// selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
		// @Override
		// public void onSelectionChange(SelectionChangeEvent event) {
		// OpsProject selected = selectionModel.getSelectedObject();
		//
		// if (selectionModel.isSelected(selected)) {
		// Room room = activity.getPlace().getRoom();
		//
		// selectionModel.setSelected(selected, false);
		// activity.addItem(room, selected);
		// }
		// }
		// });

		return table;
	}

	private static ViewUiBinder viewUiBinder = GWT.create(ViewUiBinder.class);

	private ItemListActivity activity;

	public ItemListViewImpl() {
		initWidget(viewUiBinder.createAndBindUi(this));
	}

	@Override
	public void start(ItemListActivity activity) {
		this.activity = activity;
	}

	@Override
	public CellList<UntypedItem> getItemList() {
		return itemList;
	}

	public void onItemClick(UntypedItem value) {
		activity.goTo(value);
	}
}