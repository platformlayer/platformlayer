package org.platformlayer.ui.shared.client.model;

import java.util.Set;

import javax.validation.ConstraintViolation;

import org.platformlayer.ui.shared.client.events.EditItemEvent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;
import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.EntityProxyId;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.RequestFactory;

public class ItemEditorWorkflow<P extends EntityProxy> {
	interface Binder extends UiBinder<DialogBox, ItemEditorWorkflow> {
		Binder BINDER = GWT.create(Binder.class);
	}

	public static void register(EventBus eventBus, final RequestFactory requestFactory/* , final FavoritesManager manager */) {
		eventBus.addHandler(EditItemEvent.TYPE, new EditItemEvent.Handler() {
			@Override
			public void startEdit(DomainModel model, EntityProxy person, RequestContext requestContext) {
				new ItemEditorWorkflow(model, requestFactory, /* manager */person).edit(requestContext);
			}
		});
	}

	@UiField
	HTMLPanel contents;

	@UiField
	DialogBox dialog;

	// @UiField
	// CheckBox favorite;

	@UiField(provided = true)
	Widget editorWidget;

	Editor<P> editor;

	private RequestFactoryEditorDriver<P, Editor<P>> editorDriver;
	// private final FavoritesManager manager;
	private P proxy;

	private DomainModel<P, ?> model;

	private final RequestFactory requestFactory;

	private ItemEditorWorkflow(DomainModel<P, ?> model, RequestFactory requestFactory, /* FavoritesManager manager */
			P person) {
		this.requestFactory = requestFactory;
		// this.manager = manager;
		this.proxy = person;

		this.model = model;
		// TimeSlotListWidget timeSlotEditor = new TimeSlotListWidget(requestFactory);
		// ScheduleEditor scheduleEditor = new ScheduleEditor(timeSlotEditor);
		// MentorSelector mentorEditor = new MentorSelector(requestFactory);
		this.editor = model.buildEditor();
		this.editorWidget = (Widget) this.editor;

		// new AptCacheServiceEditor(/* mentorEditor, scheduleEditor */);
		Binder.BINDER.createAndBindUi(this);
		contents.addDomHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
					onCancel(null);
				}
			}
		}, KeyUpEvent.getType());
		// this.favorite.setVisible(false);
	}

	/**
	 * Called by the cancel button when it is clicked. This method will just tear down the UI and clear the state of the
	 * workflow.
	 */
	@UiHandler("cancel")
	void onCancel(ClickEvent event) {
		dialog.hide();
	}

	/**
	 * Called by the edit dialog's save button. This method will flush the contents of the UI into the PersonProxy that
	 * is being edited, check for errors, and send the request to the server.
	 */
	@UiHandler("save")
	void onSave(ClickEvent event) {
		// Flush the contents of the UI
		RequestContext context = editorDriver.flush();

		// Check for errors
		if (editorDriver.hasErrors()) {
			dialog.setText("Errors detected locally");
			return;
		}

		// Send the request
		context.fire(new Receiver<Void>() {
			@Override
			public void onConstraintViolation(Set<ConstraintViolation<?>> errors) {
				// Otherwise, show ConstraintViolations in the UI
				dialog.setText("Errors detected on the server");
				editorDriver.setConstraintViolations(errors);
			}

			@Override
			public void onSuccess(Void response) {
				// If everything went as planned, just dismiss the dialog box
				dialog.hide();
			}
		});
	}

	// /**
	// * Called by the favorite checkbox when its value has been toggled.
	// */
	// @UiHandler("favorite")
	// void onValueChanged(ValueChangeEvent<Boolean> event) {
	// manager.setFavorite(person.stableId(), favorite.getValue());
	// }

	/**
	 * Construct and display the UI that will be used to edit the current PersonProxy, using the given RequestContext to
	 * accumulate the edits.
	 */
	private void edit(RequestContext requestContext) {
		editorDriver = model.buildEditorDriver();
		editorDriver.initialize(requestFactory, editor);

		if (requestContext == null) {
			// this.favorite.setVisible(true);
			fetchAndEdit();
			return;
		}

		editorDriver.edit(proxy, requestContext);
		// serviceInfoEditor.focus();
		// favorite.setValue(manager.isFavorite(person), false);
		dialog.center();
	}

	private void fetchAndEdit() {
		// The request is configured arbitrarily
		EntityProxyId<P> stableId = (EntityProxyId<P>) proxy.stableId();
		Request<P> fetchRequest = requestFactory.find(stableId);

		// Add the paths that the EditorDrives computes
		fetchRequest.with(editorDriver.getPaths());

		// We could do more with the request, but we just fire it
		fetchRequest.to(new Receiver<P>() {
			@Override
			public void onSuccess(P proxy) {
				ItemEditorWorkflow.this.proxy = proxy;
				// Start the edit process
				// ServiceInfoRequest context = requestFactory.serviceInfoRequest();
				// Display the UI
				edit(model.context(requestFactory));
				// Configure the method invocation to be sent in the context
				// context.persist().using(person);
				// The context will be fire()'ed from the onSave() method
			}
		}).fire();
	}
}
