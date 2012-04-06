package org.platformlayer.service.dnsresolver.client;

import org.platformlayer.service.dnsresolver.shared.DnsResolverServiceProxy;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.ui.client.ValueBoxEditorDecorator;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class DnsResolverServiceEditor extends Composite implements Editor<DnsResolverServiceProxy> {
    interface Binder extends UiBinder<Widget, DnsResolverServiceEditor> {
    }

    @UiField
    ValueBoxEditorDecorator<String> dnsName;

    public DnsResolverServiceEditor() {
        initWidget(GWT.<Binder> create(Binder.class).createAndBindUi(this));
    }

}
