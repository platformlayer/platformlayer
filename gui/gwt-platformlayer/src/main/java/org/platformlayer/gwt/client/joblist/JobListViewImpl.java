package org.platformlayer.gwt.client.joblist;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.platformlayer.gwt.client.ApplicationState;
import org.platformlayer.gwt.client.api.platformlayer.Job;
import org.platformlayer.gwt.client.job.JobPlace;
import org.platformlayer.gwt.client.view.AbstractApplicationPage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.HTMLPanel;

@Singleton
public class JobListViewImpl extends AbstractApplicationPage implements JobListView {
	interface ViewUiBinder extends UiBinder<HTMLPanel, JobListViewImpl> {
	}

	@Inject
	ApplicationState app;

	@UiField
	CellList<Job> jobList;

	@UiFactory
	CellList<Job> makeJobList() {
		CellList<Job> table = new CellList<Job>(new JobListCell(this));

		// final SingleSelectionModel<OpsProject> selectionModel = new SingleSelectionModel<Product>();
		// table.setSelectionModel(selectionModel);
		// selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
		// @Override
		// public void onSelectionChange(SelectionChangeEvent event) {
		// OpsProject selected = selectionModel.getSelectedObject();
		//
		// if (selectionModel.isSelected(selected)) {
		// Room room = activity.getPlace().getRoom();
		//
		// selectionModel.setSelected(selected, false);
		// activity.addItem(room, selected);
		// }
		// }
		// });

		return table;
	}

	private static ViewUiBinder viewUiBinder = GWT.create(ViewUiBinder.class);

	private JobListActivity activity;

	public JobListViewImpl() {
		initWidget(viewUiBinder.createAndBindUi(this));
	}

	@Override
	public void start(JobListActivity activity) {
		this.activity = activity;
	}

	@Override
	public CellList<Job> getJobList() {
		return jobList;
	}

	public void onJobClick(Job value) {
		JobPlace newPlace = activity.getJobPlace(value);
		activity.goTo(newPlace);
	}
}