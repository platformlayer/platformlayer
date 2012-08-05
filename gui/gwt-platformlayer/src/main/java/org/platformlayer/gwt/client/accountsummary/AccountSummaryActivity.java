package org.platformlayer.gwt.client.accountsummary;

import java.util.logging.Logger;

import org.platformlayer.gwt.client.ApplicationAbstractActivity;
import org.platformlayer.gwt.client.api.billing.BillingService;
import org.platformlayer.gwt.client.api.billing.ProjectBillingInfo;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class AccountSummaryActivity extends ApplicationAbstractActivity {
	static final Logger log = Logger.getLogger(AccountSummaryActivity.class.getName());

	@Inject
	AccountSummaryView view;

	@Inject
	BillingService billingService;

	AccountSummaryPlace place;

	@Override
	public void init(Place place) {
		this.place = (AccountSummaryPlace) place;
	}

	@Override
	public void start(AcceptsOneWidget panel, final EventBus eventBus) {
		view.start(this);

		panel.setWidget(view.asWidget());

		billingService.getProjectBillingInfo(getProject(), new AsyncCallback<ProjectBillingInfo>() {

			@Override
			public void onSuccess(ProjectBillingInfo result) {
				view.setModel(result);
			}

			@Override
			public void onFailure(Throwable caught) {
				// TODO: Go to app failure screen??
				view.showError(caught);
			}
		});
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public AccountSummaryPlace getPlace() {
		return place;
	}

}