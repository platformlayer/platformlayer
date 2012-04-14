package org.platformlayer.ui.shared.client.commons;

import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.RequestFactory;

public interface BasicInjector {
	EventBus getEventBus();

	RequestFactory getRequestFactory();
}
