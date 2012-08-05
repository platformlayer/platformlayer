package org.platformlayer.gwt.client.accountsummary;

import org.platformlayer.gwt.client.home.HomePlace;
import org.platformlayer.gwt.client.places.ApplicationPlace;

public class AccountSummaryPlace extends ApplicationPlace {
	public AccountSummaryPlace() {
		super(HomePlace.INSTANCE, "account");
	}

	public static final AccountSummaryPlace INSTANCE = new AccountSummaryPlace();

	@Override
	public String getLabel() {
		return "Account Summary";
	}

	@Override
	public ApplicationPlace getChild(String pathToken) {
		return null;
	}

}
