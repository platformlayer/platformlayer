package org.platformlayer.gwt.client.projectlist;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.platformlayer.gwt.client.ApplicationState;
import org.platformlayer.gwt.client.api.platformlayer.OpsProject;
import org.platformlayer.gwt.client.view.AbstractApplicationPage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.HTMLPanel;

@Singleton
public class ProjectListViewImpl extends AbstractApplicationPage implements ProjectListView {
	interface ViewUiBinder extends UiBinder<HTMLPanel, ProjectListViewImpl> {
	}

	@Inject
	ApplicationState app;

	@UiField
	CellList<OpsProject> projectList;

	private ProjectListActivity activity;

	@UiFactory
	CellList<OpsProject> makeProjectList() {
		CellList<OpsProject> table = new CellList<OpsProject>(new ProjectListCell(this));

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

	public ProjectListViewImpl() {
		initWidget(viewUiBinder.createAndBindUi(this));
	}

	@Override
	public void start(ProjectListActivity activity) {
		this.activity = activity;

		addDataDisplay(projectList, app.getProjectsProvider());
	}

	public void goToProject(OpsProject project) {
		activity.goToProject(project);
	}
}