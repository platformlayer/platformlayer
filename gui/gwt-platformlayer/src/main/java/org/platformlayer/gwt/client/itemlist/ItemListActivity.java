package org.platformlayer.gwt.client.itemlist;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.platformlayer.gwt.client.ApplicationAbstractActivity;
import org.platformlayer.gwt.client.api.platformlayer.OpsProject;
import org.platformlayer.gwt.client.api.platformlayer.PlatformLayerService;
import org.platformlayer.gwt.client.api.platformlayer.UntypedItem;
import org.platformlayer.gwt.client.api.platformlayer.UntypedItemCollection;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class ItemListActivity extends ApplicationAbstractActivity {
	static final Logger log = Logger.getLogger(ItemListActivity.class.getName());

	@Inject
	ItemListView view;

	ItemListPlace place;

	@Inject
	PlatformLayerService platformLayer;

	@Override
	public void init(Place place) {
		this.place = (ItemListPlace) place;
	}

	@Override
	public void start(AcceptsOneWidget panel, final EventBus eventBus) {
		view.start(this);

		panel.setWidget(view.asWidget());

		String parentKey = place.getParentKey();
		String projectKey = place.getProjectKey();

		OpsProject project = app.findProject(projectKey);

		platformLayer.listRoots(project, new AsyncCallback<UntypedItemCollection>() {
			@Override
			public void onSuccess(UntypedItemCollection result) {
				CellList<UntypedItem> cellList = view.getItemList();

				ListDataProvider<UntypedItem> provider = new ListDataProvider<UntypedItem>(result.getItems());
				provider.addDataDisplay(cellList);
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
}