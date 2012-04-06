package org.platformlayer.service.dns.client;

import org.platformlayer.service.dns.shared.DnsServerProxy;
import org.platformlayer.ui.shared.client.commons.TextColumn;
import org.platformlayer.ui.shared.client.model.ItemGrid;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;

public class DnsServerGrid extends ItemGrid<DnsServerProxy, DnsServerProxy.DnsServerRequest> {

    public DnsServerGrid() {
        super(DnsServerProxy.Model);
    }

    @Override
    protected void addColumns(DataGrid<DnsServerProxy> table) {
        Column<DnsServerProxy, String> dnsNameColumn = TextColumn.build(DnsServerProxy.DnsName);
        table.addColumn(dnsNameColumn, "Description");
        table.setColumnWidth(dnsNameColumn, "40ex");
    }

}
