package org.platformlayer.gwt.client.item;

import java.util.logging.Logger;

import javax.inject.Singleton;

import org.platformlayer.gwt.client.api.platformlayer.Job;
import org.platformlayer.gwt.client.api.platformlayer.Tag;
import org.platformlayer.gwt.client.api.platformlayer.UntypedItem;
import org.platformlayer.gwt.client.job.JobPlace;
import org.platformlayer.gwt.client.view.AbstractApplicationPage;
import org.platformlayer.gwt.client.widgets.Alert;
import org.platformlayer.gwt.client.widgets.AlertContainer;
import org.platformlayer.gwt.client.widgets.Repeater;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLPanel;

@Singleton
public class ItemViewImpl extends AbstractApplicationPage implements ItemView {
	static final Logger log = Logger.getLogger(ItemViewImpl.class.getName());

	interface ViewUiBinder extends UiBinder<HTMLPanel, ItemViewImpl> {
	}

	private static ViewUiBinder viewUiBinder = GWT.create(ViewUiBinder.class);

	public ItemViewImpl() {
		initWidget(viewUiBinder.createAndBindUi(this));
	}

	private ItemActivity activity;

	@UiField
	SpanElement labelSpan;

	@UiField
	Button editButton;

	@UiField
	Button configureButton;

	@UiField
	Button validateButton;

	@UiField
	Repeater<Tag> tagsList;

	@UiField
	AlertContainer alerts;

	UntypedItem model;

	@UiFactory
	public Repeater<Tag> buildTagsList() {
		Repeater<Tag> tagsList = new Repeater<Tag>(new TagCell());
		return tagsList;
	}

	@Override
	public void start(ItemActivity activity) {
		this.activity = activity;

		setModel(null);

		SafeHtml html = SafeHtmlUtils.fromString(activity.getItemPath());
		labelSpan.setInnerSafeHtml(html);
	}

	@Override
	public void setModel(UntypedItem model) {
		this.model = model;

		boolean haveModel = model != null;

		editButton.setEnabled(haveModel);

		addDataDisplay(tagsList, model.getTags());
	}

	@UiHandler("editButton")
	public void onEditButtonClick(ClickEvent e) {
		if (model == null) {
			return;
		}

		EditItemDialog dialog = new EditItemDialog();
		dialog.start(model);
	}

	@UiHandler("validateButton")
	public void onValidateButtonClick(ClickEvent e) {
		activity.doAction("validate");
	}

	@UiHandler("configureButton")
	public void onConfigureButtonClick(ClickEvent e) {
		activity.doAction("configure");
	}

	@Override
	public void showJobStartResult(Job job, Throwable e) {
		if (job != null) {
			// Success
			JobPlace place = activity.getJobPlace(job);
			alerts.add(Alert.success("Started job", place));
		} else {
			// Failure
			alerts.addError("Unable to start job", e);
		}
	}
}