package org.platformlayer.gwt.client;

import com.google.gwt.place.shared.PlaceController;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class InjectablePlaceController extends PlaceController {
	@Inject
	public InjectablePlaceController(EventBus eventBus) {
		super(eventBus);
	}
}