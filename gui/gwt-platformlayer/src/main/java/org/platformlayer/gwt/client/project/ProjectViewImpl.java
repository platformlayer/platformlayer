package org.platformlayer.gwt.client.project;

import org.platformlayer.gwt.client.view.AbstractApplicationPage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.inject.Inject;

public class ProjectViewImpl extends AbstractApplicationPage implements ProjectView {
	interface ViewUiBinder extends UiBinder<HTMLPanel, ProjectViewImpl> {
	}

	private static ViewUiBinder viewUiBinder = GWT.create(ViewUiBinder.class);

	public ProjectViewImpl() {
		initWidget(viewUiBinder.createAndBindUi(this));
	}

	@Inject
	PlaceController placeController;

	private ProjectActivity activity;

	@Override
	public void start(ProjectActivity activity) {
		this.activity = activity;
	}

	@UiHandler("jobsButton")
	public void onJobsButtonClick(ClickEvent e) {
		placeController.goTo(activity.getPlace().getJobListPlace());
	}

	@UiHandler("itemsButton")
	public void onItemButtonClick(ClickEvent e) {
		placeController.goTo(activity.getPlace().getRootPlace());
	}
}