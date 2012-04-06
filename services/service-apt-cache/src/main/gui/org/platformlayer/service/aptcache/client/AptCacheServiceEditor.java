package org.platformlayer.service.aptcache.client;

import org.platformlayer.service.aptcache.shared.AptCacheServiceProxy;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.ui.client.ValueBoxEditorDecorator;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class AptCacheServiceEditor extends Composite implements Editor<AptCacheServiceProxy> {
    interface Binder extends UiBinder<Widget, AptCacheServiceEditor> {
    }

    @UiField
    ValueBoxEditorDecorator<String> dnsName;

    public AptCacheServiceEditor() {
        initWidget(GWT.<Binder> create(Binder.class).createAndBindUi(this));
    }
}
