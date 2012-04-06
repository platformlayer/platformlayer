package org.platformlayer.service.openldap.client;

import org.platformlayer.service.openldap.shared.LdapDomainProxy;
import org.platformlayer.ui.shared.client.commons.TextColumn;
import org.platformlayer.ui.shared.client.model.ItemGrid;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;

public class LdapDomainGrid extends ItemGrid<LdapDomainProxy, LdapDomainProxy.LdapDomainRequest> {

    interface EditorDriver extends RequestFactoryEditorDriver<LdapDomainProxy, LdapDomainEditor> {

    }

    public LdapDomainGrid() {
        super(LdapDomainProxy.Model);
    }

    @Override
    protected void addColumns(DataGrid<LdapDomainProxy> table) {
        Column<LdapDomainProxy, String> dnsNameColumn = TextColumn.build(LdapDomainProxy.OrganizationName);
        table.addColumn(dnsNameColumn, "Organization Name");
        table.setColumnWidth(dnsNameColumn, "40ex");
    }

}
