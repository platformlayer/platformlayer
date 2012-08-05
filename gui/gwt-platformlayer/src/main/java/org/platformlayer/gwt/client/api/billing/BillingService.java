package org.platformlayer.gwt.client.api.billing;

import org.platformlayer.gwt.client.api.platformlayer.OpsProject;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.ImplementedBy;

@ImplementedBy(CorsBillingService.class)
public interface BillingService {
	void getProjectBillingInfo(OpsProject project, AsyncCallback<ProjectBillingInfo> callback);

	void addCreditCard(OpsProject project, CreditCardDetails card, AsyncCallback<CreditCardRecord> callback);
}
