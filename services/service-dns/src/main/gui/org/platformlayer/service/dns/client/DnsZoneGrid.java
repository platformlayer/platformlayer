package org.platformlayer.service.dns.client;

import org.platformlayer.service.dns.shared.DnsZoneProxy;
import org.platformlayer.ui.shared.client.commons.TextColumn;
import org.platformlayer.ui.shared.client.model.ItemGrid;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;

public class DnsZoneGrid extends ItemGrid<DnsZoneProxy, DnsZoneProxy.DnsZoneRequest> {

    interface EditorDriver extends RequestFactoryEditorDriver<DnsZoneProxy, DnsZoneEditor> {

    }

    public DnsZoneGrid() {
        super(DnsZoneProxy.Model);
    }

    @Override
    protected void addColumns(DataGrid<DnsZoneProxy> table) {
        Column<DnsZoneProxy, String> dnsNameColumn = TextColumn.build(DnsZoneProxy.DnsName);
        table.addColumn(dnsNameColumn, "Description");
        table.setColumnWidth(dnsNameColumn, "40ex");
    }
}
