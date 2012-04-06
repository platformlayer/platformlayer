package org.platformlayer.service.dns.client;

import org.platformlayer.service.dns.shared.DnsRecordProxy;
import org.platformlayer.ui.shared.client.commons.TextColumn;
import org.platformlayer.ui.shared.client.model.ItemGrid;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;

public class DnsRecordGrid extends ItemGrid<DnsRecordProxy, DnsRecordProxy.DnsRecordRequest> {

    interface EditorDriver extends RequestFactoryEditorDriver<DnsRecordProxy, DnsRecordEditor> {

    }

    public DnsRecordGrid() {
        super(DnsRecordProxy.Model);
    }

    @Override
    protected void addColumns(DataGrid<DnsRecordProxy> table) {
        Column<DnsRecordProxy, String> dnsNameColumn = TextColumn.build(DnsRecordProxy.DnsName);
        table.addColumn(dnsNameColumn, "Description");
        table.setColumnWidth(dnsNameColumn, "40ex");

        Column<DnsRecordProxy, String> recordTypeColumn = TextColumn.build(DnsRecordProxy.RecordType);
        table.addColumn(recordTypeColumn, "Record Type");
        table.setColumnWidth(recordTypeColumn, "40ex");

        // Column<DnsRecordProxy, String> addressColumn = TextColumn.build(DnsRecordProxy.Address);
        // table.addColumn(addressColumn, "Addresses");
        // table.setColumnWidth(addressColumn, "40ex");
    }

}
