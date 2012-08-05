package org.platformlayer.gwt.client.accountsummary;

import org.platformlayer.gwt.client.api.billing.ProjectBillingInfo;
import org.platformlayer.gwt.client.view.ApplicationView;

import com.google.inject.ImplementedBy;

@ImplementedBy(AccountSummaryViewImpl.class)
public interface AccountSummaryView extends ApplicationView {
	void start(AccountSummaryActivity homeActivity);

	void showError(Throwable caught);

	void setModel(ProjectBillingInfo model);
}
