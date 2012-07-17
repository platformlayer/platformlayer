package org.platformlayer.service.dns.client;

import org.platformlayer.service.dns.client.model.DnsZone;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TextBox;

public class DnsZoneEditor extends Composite implements Editor<DnsZone> {

	interface ViewUiBinder extends UiBinder<HTMLPanel, DnsZoneEditor> {
	}

	private static ViewUiBinder viewUiBinder = GWT.create(ViewUiBinder.class);

	public DnsZoneEditor() {
		initWidget(viewUiBinder.createAndBindUi(this));
	}

	@UiField
	TextBox dnsName;

}
