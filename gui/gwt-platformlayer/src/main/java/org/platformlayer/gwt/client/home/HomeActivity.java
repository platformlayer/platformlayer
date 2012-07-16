package org.platformlayer.gwt.client.home;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.platformlayer.gwt.client.ApplicationAbstractActivity;
import org.platformlayer.gwt.client.api.platformlayer.OpsProject;
import org.platformlayer.gwt.client.api.platformlayer.PlatformLayerService;
import org.platformlayer.gwt.client.api.platformlayer.UntypedItem;
import org.platformlayer.gwt.client.api.platformlayer.UntypedItemCollection;
import org.platformlayer.gwt.client.login.LoginPlace;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class HomeActivity extends ApplicationAbstractActivity {
	static final Logger log = Logger.getLogger(HomeActivity.class.getName());

	@Inject
	HomeView view;

	HomePlace place;

	@Inject
	PlatformLayerService platformLayer;

	@Override
	public void init(Place place) {
		this.place = (HomePlace) place;
	}

	@Override
	public void start(AcceptsOneWidget panel, final EventBus eventBus) {
		OpsProject project = app.findProject("platformlayer");
		if (project == null) {
			// TODO: Async redirect??
			placeController.goTo(LoginPlace.INSTANCE);
			return;
		}

		view.start(this);

		panel.setWidget(view.asWidget());

		platformLayer.listRoots(project, new AsyncCallback<UntypedItemCollection>() {
			@Override
			public void onSuccess(UntypedItemCollection result) {
				log.log(Level.INFO, "Success listing roots " + result);
				for (UntypedItem item : result.getItems()) {
					log.log(Level.INFO, "Item: " + item.getKey());
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				log.log(Level.WARNING, "Error listing roots", caught);
			}
		});
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public HomePlace getPlace() {
		return place;
	}
}