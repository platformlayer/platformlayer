package org.platformlayer.gwt.client.addcreditcard;

import java.util.logging.Logger;

import org.platformlayer.gwt.client.ApplicationAbstractActivity;
import org.platformlayer.gwt.client.accountsummary.AccountSummaryPlace;
import org.platformlayer.gwt.client.api.billing.BillingService;
import org.platformlayer.gwt.client.api.billing.CreditCardDetails;
import org.platformlayer.gwt.client.api.billing.CreditCardRecord;
import org.platformlayer.gwt.client.api.platformlayer.OpsProject;
import org.platformlayer.gwt.client.braintree.BraintreeClientSideEncryption;
import org.platformlayer.gwt.client.braintree.BraintreeJavascript;
import org.platformlayer.gwt.client.widgets.Alert;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class AddCreditCardActivity extends ApplicationAbstractActivity {
	static final Logger log = Logger.getLogger(AddCreditCardActivity.class.getName());

	@Inject
	AddCreditCardView view;

	@Inject
	BillingService service;

	AddCreditCardPlace place;

	@Override
	public void init(Place place) {
		this.place = (AddCreditCardPlace) place;
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

	@Override
	public AddCreditCardPlace getPlace() {
		return place;
	}

	public void addCreditCard(CreditCardDetails card) {
		BraintreeClientSideEncryption encryption = BraintreeJavascript.buildEncryptor();

		card.setCvv(encryption.encrypt(card.getCvv()));
		card.setCardNumber(encryption.encrypt(card.getCardNumber()));

		OpsProject project = getProject();
		service.addCreditCard(project, card, new AsyncCallback<CreditCardRecord>() {
			@Override
			public void onSuccess(CreditCardRecord result) {
				app.flash(Alert.success("Added credit card"));
				placeController.goTo(AccountSummaryPlace.INSTANCE);
			}

			@Override
			public void onFailure(Throwable caught) {
				view.showError(caught);
			}
		});
	}

	public void doCancel() {
		placeController.goTo(AccountSummaryPlace.INSTANCE);
	}
}