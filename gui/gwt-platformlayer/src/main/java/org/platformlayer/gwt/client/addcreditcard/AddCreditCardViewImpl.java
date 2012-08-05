package org.platformlayer.gwt.client.addcreditcard;

import javax.inject.Singleton;

import org.platformlayer.gwt.client.accountsummary.AccountSummaryPlace;
import org.platformlayer.gwt.client.api.billing.CreditCardDetails;
import org.platformlayer.gwt.client.view.AbstractApplicationPage;
import org.platformlayer.gwt.client.widgets.AlertContainer;
import org.platformlayer.gwt.client.widgets.AlertContainer.AlertLevel;
import org.platformlayer.gwt.client.widgets.ControlGroup;
import org.platformlayer.gwt.client.widgets.ToEditor;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ButtonElement;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;

@Singleton
public class AddCreditCardViewImpl extends AbstractApplicationPage implements AddCreditCardView,
		Editor<CreditCardDetails> {

	interface ViewUiBinder extends UiBinder<HTMLPanel, AddCreditCardViewImpl> {
	}

	private static ViewUiBinder viewUiBinder = GWT.create(ViewUiBinder.class);

	interface Driver extends SimpleBeanEditorDriver<CreditCardDetails, AddCreditCardViewImpl> {
	}

	Driver driver = GWT.create(Driver.class);

	public AddCreditCardViewImpl() {
		initWidget(viewUiBinder.createAndBindUi(this));

		addClickHandler(submitButton, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				activity.doCancel();
			}
		});

		addClickHandler(submitButton, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				alerts.clear();

				CreditCardDetails card = driver.flush();
				if (driver.hasErrors()) {
					// A sub-editor reported errors
					// TODO: handle this better
					return;
				}

				// card.cardNumber(cardNumber.getValue().trim());
				// card.cardholderName(cardholderName.getValue().trim());
				// card.cvv(cvv.getValue().trim());
				// String expirationDate = expirationMonth.getValue() + "/" + expirationYear.getValue();
				// card.expirationDate(expirationDate);

				// TODO: Do we need address??

				// TODO: Use JSR 303??

				// TODO: Check if already expired?
				if (Strings.isNullOrEmpty(card.getCardNumber())) {
					alerts.add(AlertLevel.Error, "Card number is required");
					return;
				}

				if (Strings.isNullOrEmpty(card.getCardholderName())) {
					alerts.add(AlertLevel.Error, "Cardholder name is required");
					return;
				}

				if (Strings.isNullOrEmpty(card.getCvv())) {
					alerts.add(AlertLevel.Error, "CVV is required");
					return;
				}

				if (Strings.isNullOrEmpty(card.getExpirationMonth())) {
					alerts.add(AlertLevel.Error, "Expiration month is required");
					return;
				}

				if (Strings.isNullOrEmpty(card.getExpirationYear())) {
					alerts.add(AlertLevel.Error, "Expiration year is required");
					return;
				}

				activity.addCreditCard(card);
			}
		});

		addClickHandler(cancelButton, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				activity.goTo(AccountSummaryPlace.INSTANCE);
			}
		});
	}

	@UiField
	ButtonElement submitButton;
	@UiField
	ButtonElement cancelButton;

	@UiField
	AlertContainer alerts;

	@UiField
	ControlGroup cardNumber;

	@UiField
	ControlGroup cardholderName;

	@UiField
	ControlGroup cvv;

	@UiField
	ToEditor expirationMonth;

	@UiField
	ToEditor expirationYear;

	private AddCreditCardActivity activity;

	@Override
	public void start(final AddCreditCardActivity activity) {
		this.activity = activity;

		alerts.clear();

		driver.initialize(this);

		CreditCardDetails defaultCard = CreditCardDetails.createObject().cast();
		driver.edit(defaultCard);
	}

	@Override
	public void showError(Throwable caught) {
		alerts.addError(caught);
	}
}