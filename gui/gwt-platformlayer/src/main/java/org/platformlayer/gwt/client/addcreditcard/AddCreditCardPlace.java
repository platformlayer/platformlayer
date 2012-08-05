package org.platformlayer.gwt.client.addcreditcard;

import org.platformlayer.gwt.client.accountsummary.AccountSummaryPlace;
import org.platformlayer.gwt.client.places.ApplicationPlace;

public class AddCreditCardPlace extends ApplicationPlace {
	public AddCreditCardPlace() {
		super(AccountSummaryPlace.INSTANCE, "addcard");
	}

	public static final AddCreditCardPlace INSTANCE = new AddCreditCardPlace();

	@Override
	public String getLabel() {
		return "Add card";
	}

	@Override
	public ApplicationPlace getChild(String pathToken) {
		return null;
	}

}
