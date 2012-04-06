package org.platformlayer.service.dns.client;

import org.platformlayer.service.dns.shared.DnsZoneProxy;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.ui.client.ValueBoxEditorDecorator;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class DnsZoneEditor extends Composite implements Editor<DnsZoneProxy> {
    interface Binder extends UiBinder<Widget, DnsZoneEditor> {
    }

    @UiField
    ValueBoxEditorDecorator<String> dnsName;

    public DnsZoneEditor() {
        initWidget(GWT.<Binder> create(Binder.class).createAndBindUi(this));
    }

}
