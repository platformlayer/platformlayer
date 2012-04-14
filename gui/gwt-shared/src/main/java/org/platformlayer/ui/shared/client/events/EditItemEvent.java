package org.platformlayer.ui.shared.client.events;

import org.platformlayer.ui.shared.client.model.DomainModel;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.RequestContext;

public class EditItemEvent extends GwtEvent<EditItemEvent.Handler> {
	public static final Type<Handler> TYPE = new Type<Handler>();

	/**
	 * Handles {@link EditItemEvent}.
	 */
	public interface Handler extends EventHandler {
		void startEdit(DomainModel model, EntityProxy person, RequestContext requestContext);
	}

	private final EntityProxy person;
	private final RequestContext request;
	private final DomainModel model;

	public EditItemEvent(DomainModel model, EntityProxy person) {
		this(model, person, null);
	}

	public EditItemEvent(DomainModel model, EntityProxy person, RequestContext request) {
		this.model = model;
		this.person = person;
		this.request = request;
	}

	@Override
	protected void dispatch(Handler handler) {
		handler.startEdit(model, person, request);
	}

	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<Handler> getAssociatedType() {
		return TYPE;
	}

	public DomainModel getModel() {
		return model;
	}
}
