package org.platformlayer.gwt.client.item;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.platformlayer.gwt.client.ApplicationAbstractActivity;
import org.platformlayer.gwt.client.api.platformlayer.Action;
import org.platformlayer.gwt.client.api.platformlayer.Job;
import org.platformlayer.gwt.client.api.platformlayer.UntypedItem;
import org.platformlayer.gwt.client.stores.ItemStore;

import com.google.gwt.core.client.JavaScriptObject;
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

		getItem(new AsyncCallback<UntypedItem>() {

			@Override
			public void onSuccess(UntypedItem result) {
				view.setModel(result);
			}

			@Override
			public void onFailure(Throwable caught) {
				log.log(Level.SEVERE, "Error retrieving item", caught);
			}
		});

	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public ItemPlace getPlace() {
		return place;
	}

	public String getItemPath() {
		return place.getItemPath();
	}

	public void getItem(AsyncCallback<UntypedItem> callback) {
		itemStore.getItem(getProject(), place.getItemPath(), callback);
	}

	public void doAction(String actionKey) {
		Action action = JavaScriptObject.createObject().cast();
		action.setName(actionKey);
		AsyncCallback<Job> callback = new AsyncCallback<Job>() {
			@Override
			public void onSuccess(Job result) {
				view.showJobStartResult(result, null);
			}

			@Override
			public void onFailure(Throwable caught) {
				view.showJobStartResult(null, caught);
			}
		};
		itemStore.doAction(getProject(), place.getItemPath(), action, callback);
	}

}