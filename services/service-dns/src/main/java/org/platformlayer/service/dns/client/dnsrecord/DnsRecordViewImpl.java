package org.platformlayer.service.dns.client.dnsrecord;

import javax.inject.Singleton;

import org.platformlayer.gwt.client.alerts.Alert;
import org.platformlayer.gwt.client.ui.ItemViewImpl;
import org.platformlayer.gwt.client.ui.ViewHandler;
import org.platformlayer.gwt.client.widgets.ControlGroup;
import org.platformlayer.gwt.client.widgets.Form;
import org.platformlayer.service.dns.model.DnsRecord;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;

@Singleton
public class DnsRecordViewImpl extends ItemViewImpl<DnsRecord> implements DnsRecordView, Editor<DnsRecord> {

	interface ViewUiBinder extends UiBinder<HTMLPanel, DnsRecordViewImpl> {
	}

	private static ViewUiBinder viewUiBinder = GWT.create(ViewUiBinder.class);

	interface Driver extends SimpleBeanEditorDriver<DnsRecord, DnsRecordViewImpl> {
	}

	Driver driver = GWT.create(Driver.class);

	public DnsRecordViewImpl() {
		initWidget(viewUiBinder.createAndBindUi(this));

		driver.initialize(this);
	}

	@UiField
	Form form;

	@UiField
	ControlGroup dnsName;
	@UiField
	ControlGroup recordType;

	private DnsRecord model;

	@Override
	public void addAlert(Alert alert, String field) {
		form.addAlert(alert, field);
	}

	@Override
	public void start(ViewHandler activity) {
		super.start(activity);

		form.clearAlerts();

		driver.edit(null);
	}

	@Override
	public void editItem(DnsRecord model) {
		this.model = model;
		driver.edit(model);
	}

	@Override
	protected Form getForm() {
		return form;
	}

	@Override
	protected void doSave() {
		form.clearAlerts();

		DnsRecord info = driver.flush();
		if (driver.hasErrors()) {
			// A sub-editor reported errors
			// TODO: handle this better
			return;
		}

		// if (Strings.isNullOrEmpty(card.getExpirationMonth())) {
		// alerts.add(AlertLevel.Error, "Expiration month is required");
		// return;
		// }

		activity.doSave(info);
	}

	@Override
	public String getViewTitle() {
		return DnsRecordPlace.LABEL;
	}

}