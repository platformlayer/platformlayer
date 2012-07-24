package org.platformlayer.gwt.client.joblist;

import org.platformlayer.gwt.client.AppTemplates;
import org.platformlayer.gwt.client.api.platformlayer.Job;
import org.platformlayer.gwt.client.api.platformlayer.JobState;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class JobListCell extends AbstractCell<Job> {
	private final JobListViewImpl view;

	public JobListCell(JobListViewImpl view) {
		super("click");
		this.view = view;
	}

	public static interface CellTemplates extends SafeHtmlTemplates {
		public static final CellTemplates INSTANCE = GWT.create(CellTemplates.class);

		@Template("<span class=\"{0}\">{1} {2}</span>")
		SafeHtml line(String rowClass, SafeHtml icon, String text);
	}

	@Override
	public void onBrowserEvent(Context context, Element parent, Job value, NativeEvent event,
			ValueUpdater<Job> valueUpdater) {
		if (NativeEvents.isClick(event)) {
			view.onJobClick(value);
		}
	}

	@Override
	public void render(Context context, Job value, SafeHtmlBuilder builder) {
		String label = value.getAction().getName() + " on " + value.getTargetId();
		SafeHtml iconHtml = SafeHtmlUtils.EMPTY_SAFE_HTML;
		JobState state = value.getState();

		switch (state) {
		case RUNNING:
			iconHtml = AppTemplates.INSTANCE.icon("icon-play");
			break;

		case FAILED:
			iconHtml = AppTemplates.INSTANCE.icon("icon-exclamation-sign");
			break;

		case SUCCESS:
			iconHtml = AppTemplates.INSTANCE.icon("icon-ok");
			break;

		default:
			assert false;
			break;
		}

		String rowClass = "";
		builder.append(CellTemplates.INSTANCE.line(rowClass, iconHtml, label));
	}
}