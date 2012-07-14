package org.platformlayer.gwt.client.projectlist;

import org.platformlayer.gwt.client.api.platformlayer.OpsProject;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiRenderer;
import com.google.gwt.user.client.Window;

public class ProjectListCell extends AbstractCell<OpsProject> {
	interface CellUiRenderer extends UiRenderer {
		void render(SafeHtmlBuilder sb, String label);

		void onBrowserEvent(ProjectListCell projectListCell, NativeEvent event, Element parent, OpsProject value);
	}

	private static CellUiRenderer renderer = GWT.create(CellUiRenderer.class);

	public ProjectListCell() {
		super("click");
	}

	@Override
	public void onBrowserEvent(Context context, Element parent, OpsProject value, NativeEvent event,
			ValueUpdater<OpsProject> valueUpdater) {
		renderer.onBrowserEvent(this, event, parent, value);
	}

	@Override
	public void render(Context context, OpsProject value, SafeHtmlBuilder builder) {
		String label = value.getProjectName();
		renderer.render(builder, label);
	}

	@UiHandler("labelSpan")
	void onLabelSpanClick(ClickEvent event, Element parent, OpsProject project) {
		Window.alert(project.getProjectName() + " was pressed!");
	}
}