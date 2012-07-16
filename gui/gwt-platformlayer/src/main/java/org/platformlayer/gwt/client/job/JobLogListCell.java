package org.platformlayer.gwt.client.job;

import org.platformlayer.gwt.client.api.platformlayer.JobLogLine;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiRenderer;

public class JobLogListCell extends AbstractCell<JobLogLine> {
	interface CellUiRenderer extends UiRenderer {
		void onBrowserEvent(JobLogListCell cell, NativeEvent event, Element parent, JobLogLine value);

		void render(SafeHtmlBuilder builder, String message);
	}

	private static CellUiRenderer renderer = GWT.create(CellUiRenderer.class);
	private final JobViewImpl view;

	public JobLogListCell(JobViewImpl view) {
		super("click");
		this.view = view;
	}

	@Override
	public void onBrowserEvent(Context context, Element parent, JobLogLine value, NativeEvent event,
			ValueUpdater<JobLogLine> valueUpdater) {
		renderer.onBrowserEvent(this, event, parent, value);
	}

	@Override
	public void render(Context context, JobLogLine value, SafeHtmlBuilder builder) {
		String message = value.getMessage();
		renderer.render(builder, message);
	}

	@UiHandler("labelSpan")
	void onLabelSpanClick(ClickEvent event, Element parent, JobLogLine value) {
		// view.onJobClick(value);
	}
}