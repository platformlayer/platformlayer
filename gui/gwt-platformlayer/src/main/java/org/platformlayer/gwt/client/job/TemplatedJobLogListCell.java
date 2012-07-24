package org.platformlayer.gwt.client.job;

import org.platformlayer.gwt.client.api.platformlayer.JobLogLine;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class TemplatedJobLogListCell extends AbstractCell<JobLogLine> {
	public static interface CellTemplates extends SafeHtmlTemplates {
		public static final CellTemplates INSTANCE = GWT.create(CellTemplates.class);

		@Template("<span class=\"{0}\">{1}</span>")
		SafeHtml jobLogLine(String css, String text);
	}

	private final JobViewImpl view;

	public TemplatedJobLogListCell(JobViewImpl view) {
		this.view = view;
	}

	@Override
	public void render(Context context, JobLogLine value, SafeHtmlBuilder builder) {
		String message = value.getMessage();
		String css = "";
		int level = value.getLevel();

		if (level >= JobLogLine.LEVEL_FATAL) {
			css = "levelFatal";
		} else if (level >= JobLogLine.LEVEL_ERROR) {
			css = "levelError";
		} else if (level >= JobLogLine.LEVEL_WARN) {
			css = "levelWarn";
		} else if (level >= JobLogLine.LEVEL_INFO) {
			css = "levelInfo";
		} else if (level >= JobLogLine.LEVEL_DEBUG) {
			css = "levelDebug";
		}

		builder.append(CellTemplates.INSTANCE.jobLogLine(css, message));
	}
}