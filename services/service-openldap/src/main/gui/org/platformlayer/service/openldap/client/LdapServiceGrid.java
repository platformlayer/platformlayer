package org.platformlayer.service.openldap.client;

import org.platformlayer.service.openldap.shared.LdapServiceProxy;
import org.platformlayer.ui.shared.client.commons.TextColumn;
import org.platformlayer.ui.shared.client.model.ItemGrid;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;

public class LdapServiceGrid extends ItemGrid<LdapServiceProxy, LdapServiceProxy.LdapServiceRequest> {

    public LdapServiceGrid() {
        super(LdapServiceProxy.Model);
    }

    @Override
    protected void addColumns(DataGrid<LdapServiceProxy> table) {
        Column<LdapServiceProxy, String> dnsNameColumn = TextColumn.build(LdapServiceProxy.DnsName);
        table.addColumn(dnsNameColumn, "Description");
        table.setColumnWidth(dnsNameColumn, "40ex");
    }

}
