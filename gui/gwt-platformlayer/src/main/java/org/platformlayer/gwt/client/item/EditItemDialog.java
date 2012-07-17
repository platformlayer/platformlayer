package org.platformlayer.gwt.client.item;

import java.util.logging.Logger;

import org.platformlayer.gwt.client.api.platformlayer.UntypedItem;
import org.platformlayer.service.dns.client.DnsZoneEditor;
import org.platformlayer.service.dns.client.DnsZoneItemActivity;
import org.platformlayer.service.dns.client.model.DnsZone;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.PopupPanel;

public class EditItemDialog extends Composite {
	static final Logger log = Logger.getLogger(EditItemDialog.class.getName());

	interface ViewUiBinder extends UiBinder<HTMLPanel, EditItemDialog> {
	}

	private static ViewUiBinder viewUiBinder = GWT.create(ViewUiBinder.class);

	public EditItemDialog() {
		initWidget(viewUiBinder.createAndBindUi(this));
		sinkEvents(Event.getTypeInt("click"));
	}

	@UiField
	HTMLPanel body;

	@UiField
	Element closeButton;

	@UiField
	Element saveButton;

	private UntypedItem item;

	public void start(UntypedItem item) {
		this.item = item;

		DnsZone dnsZone = item.cast();

		DnsZoneItemActivity activity = new DnsZoneItemActivity();
		DnsZoneEditor editor = activity.edit(dnsZone);

		body.add(editor);

		PopupPanel panel = new PopupPanel();
		panel.add(this);
		panel.center();
	}

	// @UiHandler("closeButton")
	void onCloseButtonClick(/* ClickEvent event */) {
		Window.alert("onCloseButton clicked");
	}

	// @UiHandler("saveButton")
	void onSaveButtonClick(/* ClickEvent event */) {
		Window.alert("onSaveButtonClick clicked");
	}

	@Override
	public void onBrowserEvent(Event event) {
		super.onBrowserEvent(event);

		// TODO: Cleanup or convert to generator??
		// TODO: is Event.getTypeInt inlined?
		if (event.getTypeInt() == Event.getTypeInt("click")) {
			EventTarget eventTarget = event.getEventTarget();
			if (Element.is(eventTarget)) {
				Element target = eventTarget.cast();
				if (target == closeButton) {
					onCloseButtonClick();
					event.preventDefault();
				} else if (target == saveButton) {
					onSaveButtonClick();
					event.preventDefault();
				}
			}
		}
	}

}