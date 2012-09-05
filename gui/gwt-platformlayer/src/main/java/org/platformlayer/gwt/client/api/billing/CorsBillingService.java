package org.platformlayer.gwt.client.api.billing;

import java.util.logging.Logger;

import org.platformlayer.gwt.client.api.platformlayer.AuthenticatedCorsRequest;
import org.platformlayer.gwt.client.api.platformlayer.OpsProject;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class CorsBillingService implements BillingService {
	static final Logger log = Logger.getLogger(CorsBillingService.class.getName());

	@Override
	public void getProjectBillingInfo(OpsProject project, AsyncCallback<ProjectBillingInfo> callback) {
		String url = project.getBillingProjectBaseUrl();
		AuthenticatedCorsRequest.get(project, url).execute(callback);
	}

	@Override
	public void addCreditCard(OpsProject project, CreditCardDetails card, AsyncCallback<CreditCardRecord> callback) {
		String url = project.getBillingProjectBaseUrl() + "card";
		String json = card.toJson();
		AuthenticatedCorsRequest.post(project, url, json).execute(callback);
	}
}
