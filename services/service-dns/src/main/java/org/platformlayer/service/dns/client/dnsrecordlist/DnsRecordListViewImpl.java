package org.platformlayer.service.dns.client.dnsrecordlist;

import javax.inject.Singleton;

import org.platformlayer.gwt.client.NativeEvents;
import org.platformlayer.gwt.client.widgets.BoundTable;
import org.platformlayer.service.dns.client.model.DnsRecord;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ButtonElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.HasData;

@Singleton
public class DnsRecordListViewImpl extends ListViewImpl<DnsRecord> implements DnsRecordListView {
	interface ViewUiBinder extends UiBinder<HTMLPanel, DnsRecordListViewImpl> {
	}

	@UiField
	CellTable<DnsRecord> recordList;

	@UiField
	ButtonElement addButton;

	@UiFactory
	CellTable<DnsRecord> makeDomainList() {
		final BoundTable<DnsRecord> table = new BoundTable<DnsRecord>();

		Column<DnsRecord, String> dnsName = JsTextColumn.build("dnsName");
		table.addColumn(dnsName, "DNS Name");

		Column<DnsRecord, String> recordType = JsTextColumn.build("recordType");
		table.addColumn(recordType, "Record Type");

		Column<DnsRecord, String> addresses = JsArrayTextColumn.build("address");
		table.addColumn(addresses, "Addresses");

		// TODO: Refactor
		table.addCellPreviewHandler(new CellPreviewEvent.Handler<DnsRecord>() {
			@Override
			public void onCellPreview(CellPreviewEvent<DnsRecord> event) {
				boolean isClick = NativeEvents.isClick(event.getNativeEvent());
				if (!isClick) {
					return;
				}

				// int column = event.getColumn();
				// if (column == 2) {
				// // Ignore the jobs column
				// return;
				// }
				DnsRecord domainName = event.getValue();
				if (domainName == null) {
					return;
				}

				onRowClick(domainName);
			}
		});

		return table;
	}

	protected void onRowClick(DnsRecord record) {
		activity.goTo(record);
	}

	private static ViewUiBinder viewUiBinder = GWT.create(ViewUiBinder.class);

	public DnsRecordListViewImpl() {
		initWidget(viewUiBinder.createAndBindUi(this));

		addClickHandler(addButton, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				activity.addNew();
			}
		});
	}

	@Override
	public HasData<DnsRecord> getList() {
		return recordList;
	}

}