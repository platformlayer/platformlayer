package org.platformlayer.gwt.client.item;

import java.util.logging.Logger;

import org.platformlayer.gwt.client.ApplicationAbstractActivity;
import org.platformlayer.gwt.client.api.platformlayer.UntypedItem;
import org.platformlayer.gwt.client.stores.ItemStore;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class ItemActivity extends ApplicationAbstractActivity {
	static final Logger log = Logger.getLogger(ItemActivity.class.getName());

	@Inject
	ItemView view;

	ItemPlace place;

	@Inject
	ItemStore itemStore;

	@Override
	public void init(Place place) {
		this.place = (ItemPlace) place;
	}

	@Override
	public void start(AcceptsOneWidget panel, final EventBus eventBus) {
		view.start(this);

		panel.setWidget(view.asWidget());
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	public ItemPlace getPlace() {
		return place;
	}

	public String getItemPath() {
		return place.getItemPath();
	}

	public void getItem(AsyncCallback<UntypedItem> callback) {
		itemStore.getItem(getProject(), place.getItemPath(), callback);
	}

}