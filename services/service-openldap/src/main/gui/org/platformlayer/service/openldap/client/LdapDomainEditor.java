package org.platformlayer.service.openldap.client;

import org.platformlayer.service.openldap.shared.LdapDomainProxy;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.ui.client.ValueBoxEditorDecorator;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class LdapDomainEditor extends Composite implements Editor<LdapDomainProxy> {
    interface Binder extends UiBinder<Widget, LdapDomainEditor> {
    }

    @UiField
    ValueBoxEditorDecorator<String> organizationName;

    public LdapDomainEditor() {
        initWidget(GWT.<Binder> create(Binder.class).createAndBindUi(this));
    }
}
