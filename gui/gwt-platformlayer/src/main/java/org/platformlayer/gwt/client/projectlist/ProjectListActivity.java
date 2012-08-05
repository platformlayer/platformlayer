package org.platformlayer.gwt.client.projectlist;

import java.util.logging.Logger;

import org.platformlayer.gwt.client.ApplicationAbstractActivity;
import org.platformlayer.gwt.client.api.platformlayer.OpsProject;
import org.platformlayer.gwt.client.api.platformlayer.PlatformLayerService;
import org.platformlayer.gwt.client.signin.SignInPlace;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class ProjectListActivity extends ApplicationAbstractActivity {
	static final Logger log = Logger.getLogger(ProjectListActivity.class.getName());

	@Inject
	ProjectListView view;

	ProjectListPlace place;

	@Inject
	PlatformLayerService platformLayer;

	@Override
	public void init(Place place) {
		this.place = (ProjectListPlace) place;
	}

	@Override
	public void start(AcceptsOneWidget panel, final EventBus eventBus) {
		OpsProject project = app.findProject("production");
		if (project == null) {
			// TODO: Async redirect??
			placeController.goTo(SignInPlace.INSTANCE);
			return;
		}

		view.start(this);

		panel.setWidget(view.asWidget());

		// platformLayer.listRoots(project, new AsyncCallback<UntypedItemCollection>() {
		// @Override
		// public void onSuccess(UntypedItemCollection result) {
		// log.log(Level.INFO, "Success listing roots " + result);
		// for (UntypedItem item : result.getItems()) {
		// log.log(Level.INFO, "Item: " + item.getKey());
		// }
		// }
		//
		// @Override
		// public void onFailure(Throwable caught) {
		// log.log(Level.WARNING, "Error listing roots", caught);
		// }
		// });
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	public void goToProject(OpsProject project) {
		Place newPlace = ProjectListPlace.INSTANCE.getProjectPlace(project.getProjectName());
		placeController.goTo(newPlace);
	}

	@Override
	public ProjectListPlace getPlace() {
		return place;
	}
}