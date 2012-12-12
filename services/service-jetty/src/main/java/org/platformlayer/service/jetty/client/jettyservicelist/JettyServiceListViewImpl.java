package org.platformlayer.service.jetty.client.jettyservicelist;

import javax.inject.Singleton;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.gwt.client.ui.DefaultListViewImpl;
import org.platformlayer.gwt.client.ui.JsTextColumn;
import org.platformlayer.gwt.client.widgets.BoundTable;
import org.platformlayer.service.jetty.model.JettyService;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;

@Singleton
public class JettyServiceListViewImpl extends DefaultListViewImpl<JettyService> {
	@Override
	protected void populateColumns(BoundTable<JettyService> table) {

		Column<JettyService, String> idColumn = new TextColumn<JettyService>() {
			@Override
			public String getValue(JettyService object) {
				PlatformLayerKey key = object.getKey();
				if (key == null) {
					return "";
				}
				String id = key.getItemIdString();
				if (id == null) {
					return "";
				}
				return id;
			}
		};
		table.addColumn(idColumn, "ID");

		Column<JettyService, String> dnsName = JsTextColumn.build("dnsName");
		table.addColumn(dnsName, "DNS Name");
	}
}