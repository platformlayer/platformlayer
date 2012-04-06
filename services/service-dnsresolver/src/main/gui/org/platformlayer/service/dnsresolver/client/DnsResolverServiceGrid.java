package org.platformlayer.service.dnsresolver.client;

import org.platformlayer.service.dnsresolver.shared.DnsResolverServiceProxy;
import org.platformlayer.ui.shared.client.commons.TextColumn;
import org.platformlayer.ui.shared.client.model.ItemGrid;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;

public class DnsResolverServiceGrid extends ItemGrid<DnsResolverServiceProxy, DnsResolverServiceProxy.DnsResolverServiceRequest> {

    interface EditorDriver extends RequestFactoryEditorDriver<DnsResolverServiceProxy, DnsResolverServiceEditor> {

    }

    public DnsResolverServiceGrid() {
        super(DnsResolverServiceProxy.Model);
    }

    @Override
    protected void addColumns(DataGrid<DnsResolverServiceProxy> table) {
        Column<DnsResolverServiceProxy, String> dnsNameColumn = TextColumn.build(DnsResolverServiceProxy.DnsName);
        table.addColumn(dnsNameColumn, "Description");
        table.setColumnWidth(dnsNameColumn, "40ex");
    }

}
