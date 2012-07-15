package org.platformlayer.gwt.client.joblist;

import org.platformlayer.gwt.client.api.platformlayer.Job;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiRenderer;

public class JobListCell extends AbstractCell<Job> {
	interface CellUiRenderer extends UiRenderer {
		void render(SafeHtmlBuilder sb, String label, String state);

		void onBrowserEvent(JobListCell cell, NativeEvent event, Element parent, Job value);
	}

	private static CellUiRenderer renderer = GWT.create(CellUiRenderer.class);
	private final JobListViewImpl view;

	public JobListCell(JobListViewImpl view) {
		super("click");
		this.view = view;
	}

	@Override
	public void onBrowserEvent(Context context, Element parent, Job value, NativeEvent event,
			ValueUpdater<Job> valueUpdater) {
		renderer.onBrowserEvent(this, event, parent, value);
	}

	@Override
	public void render(Context context, Job value, SafeHtmlBuilder builder) {
		String label = value.getKey();
		String state = value.getState();
		renderer.render(builder, label, state);
	}

	@UiHandler("labelSpan")
	void onLabelSpanClick(ClickEvent event, Element parent, Job value) {
		view.onJobClick(value);
	}
}