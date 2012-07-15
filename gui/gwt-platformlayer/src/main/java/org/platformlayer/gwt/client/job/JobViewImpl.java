package org.platformlayer.gwt.client.job;

import org.platformlayer.gwt.client.api.platformlayer.Job;
import org.platformlayer.gwt.client.view.AbstractApplicationPage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;

public class JobViewImpl extends AbstractApplicationPage implements JobView {
	interface ViewUiBinder extends UiBinder<HTMLPanel, JobViewImpl> {
	}

	private static ViewUiBinder viewUiBinder = GWT.create(ViewUiBinder.class);

	public JobViewImpl() {
		initWidget(viewUiBinder.createAndBindUi(this));
	}

	private JobActivity activity;

	@UiField
	SpanElement labelSpan;

	@Override
	public void start(JobActivity activity) {
		this.activity = activity;

		Job job = activity.getJobState();

		SafeHtml html = SafeHtmlUtils.fromString(job.getTargetId() + " " + job.getAction().getName());
		labelSpan.setInnerSafeHtml(html);
	}

}