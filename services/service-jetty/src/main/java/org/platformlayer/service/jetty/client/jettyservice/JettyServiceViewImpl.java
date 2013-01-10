package org.platformlayer.service.jetty.client.jettyservice;

import javax.inject.Singleton;

import org.platformlayer.gwt.client.metrics.MetricPlace;
import org.platformlayer.gwt.client.ui.ItemViewImpl;
import org.platformlayer.gwt.client.ui.ViewHandler;
import org.platformlayer.gwt.client.widgets.ControlGroup;
import org.platformlayer.gwt.client.widgets.Form;
import org.platformlayer.service.jetty.model.JettyService;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;

@Singleton
public class JettyServiceViewImpl extends ItemViewImpl<JettyService> implements JettyServiceView, Editor<JettyService> {

	interface ViewUiBinder extends UiBinder<HTMLPanel, JettyServiceViewImpl> {
	}

	private static ViewUiBinder viewUiBinder = GWT.create(ViewUiBinder.class);

	interface Driver extends SimpleBeanEditorDriver<JettyService, JettyServiceViewImpl> {
	}

	Driver driver = GWT.create(Driver.class);

	public JettyServiceViewImpl() {
		initWidget(viewUiBinder.createAndBindUi(this));

		driver.initialize(this);

		fillStandardUi(actions);
	}

	@UiField
	FlowPanel actions;

	@UiField
	Form form;

	@UiField
	ControlGroup dnsName;

	private JettyService model;

	@Override
	public void start(ViewHandler activity) {
		super.start(activity);

		form.clearAlerts();

		driver.edit(null);
	}

	@Override
	public void editItem(JettyService model) {
		super.editItem(model);

		this.model = model;

		driver.edit(model);
	}

	@UiHandler("metricButton")
	public void onMetricButton(ClickEvent e) {
		MetricPlace metricPlace = new MetricPlace(activity.getPlace(), "jvm");
		activity.goTo(metricPlace);
	}

	@Override
	protected Form getForm() {
		return form;
	}

	@Override
	protected void doSave() {
		form.clearAlerts();

		JettyService info = driver.flush();
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
		return JettyServicePlace.LABEL;
	}

}