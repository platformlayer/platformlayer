package org.platformlayer.gwt.client.addcreditcard;

import org.platformlayer.gwt.client.view.ApplicationView;

import com.google.inject.ImplementedBy;

@ImplementedBy(AddCreditCardViewImpl.class)
public interface AddCreditCardView extends ApplicationView {
	void start(AddCreditCardActivity homeActivity);

	void showError(Throwable caught);
}
