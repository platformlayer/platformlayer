package org.platformlayer.gwt.client.item;

import org.platformlayer.gwt.client.view.AbstractApplicationPage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;

public class ItemViewImpl extends AbstractApplicationPage implements ItemView {
	interface ViewUiBinder extends UiBinder<HTMLPanel, ItemViewImpl> {
	}

	private static ViewUiBinder viewUiBinder = GWT.create(ViewUiBinder.class);

	public ItemViewImpl() {
		initWidget(viewUiBinder.createAndBindUi(this));
	}

	private ItemActivity activity;

	@UiField
	SpanElement labelSpan;

	@Override
	public void start(ItemActivity activity) {
		this.activity = activity;

		SafeHtml html = SafeHtmlUtils.fromString(activity.getItemPath());
		labelSpan.setInnerSafeHtml(html);
	}

}