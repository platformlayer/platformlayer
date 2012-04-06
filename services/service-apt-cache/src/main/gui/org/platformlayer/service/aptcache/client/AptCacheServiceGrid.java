package org.platformlayer.service.aptcache.client;

import org.platformlayer.service.aptcache.shared.AptCacheServiceProxy;
import org.platformlayer.ui.shared.client.commons.TextColumn;
import org.platformlayer.ui.shared.client.model.ItemGrid;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;

public class AptCacheServiceGrid extends ItemGrid<AptCacheServiceProxy, AptCacheServiceProxy.AptCacheServiceRequest> {

    public AptCacheServiceGrid() {
        super(AptCacheServiceProxy.Model);
    }

    @Override
    protected void addColumns(DataGrid<AptCacheServiceProxy> table) {
        Column<AptCacheServiceProxy, String> dnsNameColumn = TextColumn.build(AptCacheServiceProxy.DnsName);
        table.addColumn(dnsNameColumn, "Description");
        table.setColumnWidth(dnsNameColumn, "40ex");
    }

}
