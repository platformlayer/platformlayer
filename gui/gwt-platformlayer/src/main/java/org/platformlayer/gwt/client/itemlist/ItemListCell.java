package org.platformlayer.gwt.client.itemlist;

import org.platformlayer.gwt.client.api.platformlayer.UntypedItem;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiRenderer;

public class ItemListCell extends AbstractCell<UntypedItem> {
	interface CellUiRenderer extends UiRenderer {
		void render(SafeHtmlBuilder sb, String label);

		void onBrowserEvent(ItemListCell cell, NativeEvent event, Element parent, UntypedItem value);
	}

	private static CellUiRenderer renderer = GWT.create(CellUiRenderer.class);
	private final ItemListViewImpl view;

	public ItemListCell(ItemListViewImpl view) {
		super("click");
		this.view = view;
	}

	@Override
	public void onBrowserEvent(Context context, Element parent, UntypedItem value, NativeEvent event,
			ValueUpdater<UntypedItem> valueUpdater) {
		renderer.onBrowserEvent(this, event, parent, value);
	}

	@Override
	public void render(Context context, UntypedItem value, SafeHtmlBuilder builder) {
		String label = value.getKey();
		renderer.render(builder, label);
	}

	@UiHandler("labelSpan")
	void onLabelSpanClick(ClickEvent event, Element parent, UntypedItem value) {
		view.onItemClick(value);
	}
}