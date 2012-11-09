package org.platformlayer.service.jetty.client.jettyservice;

import javax.inject.Inject;

import org.platformlayer.common.IsItem;
import org.platformlayer.core.model.Action;
import org.platformlayer.core.model.ConfigureAction;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.ValidateAction;
import org.platformlayer.gwt.client.async.AsyncWidget;
import org.platformlayer.gwt.client.events.JobExecutionEvent;
import org.platformlayer.gwt.client.jobs.JobSummary;
import org.platformlayer.gwt.client.ui.ItemActivity;
import org.platformlayer.gwt.client.ui.ItemView;
import org.platformlayer.gwt.client.ui.ViewHandler;
import org.platformlayer.gwt.client.widgets.ActionsWidget;
import org.platformlayer.jobs.model.JobData;
import org.platformlayer.jobs.model.JobExecutionData;
import org.platformlayer.ui.shared.client.views.AbstractApplicationView;

import com.google.gwt.dom.client.Element;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

public abstract class ItemViewImpl<T extends IsItem> extends AbstractApplicationView implements ItemView<T>, Editor<T> {

	protected ItemActivity<?, ?, T> activity;
	private AsyncWidget jobSummary;
	protected T model;

	@Inject
	EventBus eventBus;

	// interface ViewUiBinder extends UiBinder<HTMLPanel, ItemViewImpl> {
	// }
	//
	// private static ViewUiBinder viewUiBinder = GWT.create(ViewUiBinder.class);
	//
	// interface Driver extends SimpleBeanEditorDriver<JettyService, ItemViewImpl> {
	// }
	//
	// Driver driver = GWT.create(Driver.class);
	//
	// public ItemViewImpl() {
	// initWidget(viewUiBinder.createAndBindUi(this));
	//
	// addClickHandler(submitButton, new ClickHandler() {
	// @Override
	// public void onClick(ClickEvent event) {
	// form.clearAlerts();
	//
	// JettyService info = driver.flush();
	// if (driver.hasErrors()) {
	// // A sub-editor reported errors
	// // TODO: handle this better
	// return;
	// }
	//
	// // if (Strings.isNullOrEmpty(card.getExpirationMonth())) {
	// // alerts.add(AlertLevel.Error, "Expiration month is required");
	// // return;
	// // }
	//
	// activity.doSave(info);
	// }
	// });
	//
	// addClickHandler(cancelButton, new ClickHandler() {
	// @Override
	// public void onClick(ClickEvent event) {
	// activity.doCancel();
	// }
	// });
	//
	// driver.initialize(this);
	// }
	//
	// @UiField
	// ActionsWidget actionsWidget;
	//
	// @UiField
	// ButtonElement submitButton;
	// @UiField
	// ButtonElement cancelButton;
	//
	// @UiField
	// Form form;
	//
	// @UiField
	// ControlGroup dnsName;
	//
	// private JettyServiceActivity activity;
	//
	// private JettyService model;
	//
	// @Override
	// public void addAlert(Alert alert, String field) {
	// form.addAlert(alert, field);
	// }
	//

	private boolean initialized;
	private HandlerRegistration jobListener;

	@Override
	public void start(ViewHandler activity) {
		if (!initialized) {
			initialize();
			initialized = true;
		}
		this.activity = (ItemActivity<?, ?, T>) activity;
		//
		// form.clearAlerts();
		//
		// driver.edit(null);
		// // TODO: Any way to use editor framework??
		// actionsWidget.show(null);
	}

	private void initialize() {
		// assert this.itemListener == null;
		// this.itemListener = ManagedItemEvent.registerForEvents(eventBus, new ManagedItemEvent.Handler() {
		// @Override
		// public void onChange(ManagedItemEvent event) {
		// IsItem item = event.getItem();
		// PlatformLayerKey itemKey = item.getKey();
		// onEvent(itemKey, item);
		// }
		// });

		assert this.jobListener == null;
		this.jobListener = JobExecutionEvent.registerForEvents(eventBus, new JobExecutionEvent.Handler() {
			@Override
			public void onChange(JobExecutionEvent event) {
				JobExecutionData execution = event.getJobExecution();
				JobData job = execution.getJob();
				PlatformLayerKey itemKey = job.getTargetItemKey();
				onEvent(itemKey, null);
			}
		});
	}

	protected void onEvent(PlatformLayerKey eventKey, IsItem newItem) {
		if (model == null) {
			return;
		}

		PlatformLayerKey modelKey = model.getKey();
		if (!modelKey.equals(eventKey)) {
			return;
		}

		refreshJobs();
	}

	// @Override
	// public void editItem(JettyService model) {
	// this.model = model;
	//
	// actionsWidget.show(model);
	// driver.edit(model);
	// }
	//
	// @UiHandler("metricButton")
	// public void onMetricButton(ClickEvent e) {
	// MetricPlace metricPlace = new MetricPlace(activity.getPlace(), "jvm");
	// activity.goTo(metricPlace);
	// }

	protected void fillStandardUi(FlowPanel container) {
		ActionsWidget actions = buildActionsUi();
		container.add(actions);

		this.jobSummary = new AsyncWidget() {
			@Override
			protected void onAction(Element target, String action) {
				String id = target.getAttribute("data-id");
				activity.onAction(action, id);
			}
		};
		container.add(jobSummary);
	}

	protected ActionsWidget buildActionsUi() {
		ActionsWidget actionsWidget = new ActionsWidget();
		addActions(actionsWidget);

		actionsWidget.setListener(new ActionsWidget.ActionListener() {
			@Override
			public void fireAction(Action action) {
				onAction(action);
			}
		});

		return actionsWidget;
	}

	private void addActions(ActionsWidget actionsWidget) {
		actionsWidget.addAction(ConfigureAction.create());
		actionsWidget.addAction(ValidateAction.create());
	}

	protected void onAction(Action action) {
		activity.doAction(action);
	}

	@Override
	public void editItem(T model) {
		this.model = model;
		refreshJobs();
	}

	protected void refreshJobs() {
		jobSummary.start(model != null ? new JobSummary(model) : null);
	}
}