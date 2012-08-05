package org.platformlayer.gwt.client.accountsummary;

import javax.inject.Singleton;

import org.platformlayer.gwt.client.addcreditcard.AddCreditCardPlace;
import org.platformlayer.gwt.client.api.billing.ProjectBillingInfo;
import org.platformlayer.gwt.client.view.AbstractApplicationPage;
import org.platformlayer.gwt.client.widgets.AlertContainer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ButtonElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

@Singleton
public class AccountSummaryViewImpl extends AbstractApplicationPage implements AccountSummaryView {

	interface ViewUiBinder extends UiBinder<HTMLPanel, AccountSummaryViewImpl> {
	}

	private static ViewUiBinder viewUiBinder = GWT.create(ViewUiBinder.class);

	public AccountSummaryViewImpl() {
		initWidget(viewUiBinder.createAndBindUi(this));
	}

	@UiField
	AlertContainer alerts;

	@UiField
	Label balanceLabel;

	@UiField
	ButtonElement addCardButton;

	@Override
	public void start(final AccountSummaryActivity activity) {
		alerts.clear();
		setModel(null);

		addClickHandler(addCardButton, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				activity.goTo(AddCreditCardPlace.INSTANCE);
			}
		});
	}

	@Override
	public void showError(Throwable caught) {
		alerts.addError(caught);
	}

	@Override
	public void setModel(ProjectBillingInfo model) {
		String balanceLabelText = "";

		if (model != null) {
			float balance = model.balance();

			NumberFormat fmt = NumberFormat.getFormat("$ ###,###,##0.00");
			balanceLabelText = fmt.format(balance);
		}

		balanceLabel.setText(balanceLabelText);
	}
}