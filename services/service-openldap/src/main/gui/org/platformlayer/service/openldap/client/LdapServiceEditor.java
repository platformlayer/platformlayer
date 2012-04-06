package org.platformlayer.service.openldap.client;

import org.platformlayer.service.openldap.shared.LdapServiceProxy;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.ui.client.ValueBoxEditorDecorator;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class LdapServiceEditor extends Composite implements Editor<LdapServiceProxy> {
    interface Binder extends UiBinder<Widget, LdapServiceEditor> {
    }

    @UiField
    ValueBoxEditorDecorator<String> dnsName;

    @UiField
    ValueBoxEditorDecorator<String> ldapServerPassword;

    public LdapServiceEditor() {
        initWidget(GWT.<Binder> create(Binder.class).createAndBindUi(this));
    }
}
