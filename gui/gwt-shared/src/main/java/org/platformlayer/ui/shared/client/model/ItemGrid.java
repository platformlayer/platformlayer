package org.platformlayer.ui.shared.client.model;

import java.util.Collections;
import java.util.List;

import org.platformlayer.ui.shared.client.commons.Injection;
import org.platformlayer.ui.shared.client.events.EditItemEvent;
import org.platformlayer.ui.shared.shared.BaseEntityRequest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RangeChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.EntityProxyChange;
import com.google.web.bindery.requestfactory.shared.EntityProxyId;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.RequestFactory;
import com.google.web.bindery.requestfactory.shared.WriteOperation;

public abstract class ItemGrid<P extends EntityProxy, Context extends BaseEntityRequest<P>> extends Composite implements
		RequiresResize {

	protected final SingleSelectionModel<P> selectionModel = new SingleSelectionModel<P>();

	interface Binder extends UiBinder<Widget, ItemGrid> {
	}

	public interface Style extends CssResource {
	}

	public interface TableResources extends DataGrid.Resources {
		@Override
		@Source(value = { DataGrid.Style.DEFAULT_CSS, "DataGridPatch.css" })
		DataGrid.Style dataGridStyle();
	}

	protected int offsetOf(EntityProxyId<P> personId) {
		List<P> displayedItems = table.getVisibleItems();
		for (int offset = 0, j = displayedItems.size(); offset < j; offset++) {
			if (personId.equals(displayedItems.get(offset).stableId())) {
				return offset;
			}
		}
		return -1;
	}

	protected void refreshSelection() {
		P person = selectionModel.getSelectedObject();
		if (person == null) {
			return;
		}
		// eventBus.fireEvent(new EditPersonEvent(person));
		selectionModel.setSelected(person, false);
	}

	@UiField
	DockLayoutPanel dock;

	@UiField(provided = true)
	SimplePager pager = new SimplePager();

	@UiField(provided = true)
	DataGrid<P> table;

	@UiField
	Button create;

	private final EventBus eventBus;
	private int lastFetch;
	private final int numRows = 20;
	private final RequestFactory requestFactory;

	private final DomainModel<P, Context> model;

	public ItemGrid(DomainModel<P, Context> model) {
		this.model = model;

		this.eventBus = Injection.injector().getEventBus();
		this.requestFactory = Injection.injector().getRequestFactory();

		table = new DataGrid<P>(numRows, GWT.<TableResources> create(TableResources.class));
		initWidget(GWT.<Binder> create(Binder.class).createAndBindUi(this));

		create.setText("New " + model.getClass().toString());

		addColumns(table);

		// table.addColumn(new ScheduleColumn(), "Schedule");
		table.setRowCount(numRows, false);
		table.setSelectionModel(selectionModel);
		table.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);

		EntityProxyChange.registerForProxyType(eventBus, model.getProxyClass(), new EntityProxyChange.Handler<P>() {
			@Override
			public void onProxyChange(EntityProxyChange<P> event) {
				ItemGrid.this.onPersonChanged(event);
			}
		});

		// FilterChangeEvent.register(eventBus, new FilterChangeEvent.Handler() {
		// @Override
		// public void onFilterChanged(FilterChangeEvent e) {
		// filter.set(e.getDay(), e.isSelected());
		// if (!pending) {
		// pending = true;
		// Scheduler.get().scheduleFinally(new ScheduledCommand() {
		// @Override
		// public void execute() {
		// pending = false;
		// fetch(0);
		// }
		// });
		// }
		// }
		// });

		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				ItemGrid.this.refreshSelection();
			}
		});

		fetch(0);
	}

	protected abstract void addColumns(DataGrid<P> table);

	@UiHandler("create")
	void onCreate(ClickEvent event) {
		Context context = model.context(requestFactory);

		P item = context.edit(context.create(model.getProxyClass()));
		model.persist(context, item);
		eventBus.fireEvent(new EditItemEvent(model, item, context));
	}

	void onPersonChanged(EntityProxyChange<P> event) {
		if (WriteOperation.PERSIST.equals(event.getWriteOperation())) {
			// Re-fetch if we're already displaying the last page
			if (table.isRowCountExact()) {
				fetch(lastFetch + 1);
			}
		}
		if (WriteOperation.UPDATE.equals(event.getWriteOperation())) {
			EntityProxyId<P> personId = event.getProxyId();

			// Is the changing record onscreen?
			int displayOffset = offsetOf(personId);
			if (displayOffset != -1) {
				// Record is onscreen and may differ from our data
				requestFactory.find(personId).fire(new Receiver<P>() {
					@Override
					public void onSuccess(P person) {
						// Re-check offset in case of changes while waiting for data
						EntityProxyId<P> stableId = (EntityProxyId<P>) person.stableId();
						int offset = offsetOf(stableId);
						if (offset != -1) {
							table.setRowData(table.getPageStart() + offset, Collections.singletonList(person));
						}
					}
				});
			}
		}
	}

	@UiHandler("table")
	void onRangeChange(RangeChangeEvent event) {
		Range r = event.getNewRange();
		int start = r.getStart();

		fetch(start);
	}

	private void fetch(final int start) {
		lastFetch = start;
		model.findAll(requestFactory).fire(new Receiver<List<P>>() {
			@Override
			public void onSuccess(List<P> response) {
				if (lastFetch != start) {
					return;
				}

				int responses = response.size();
				table.setRowData(start, response);
				pager.setPageStart(start);
				if (start == 0 || !table.isRowCountExact()) {
					table.setRowCount(start + responses, responses < numRows);
				}
			}
		});
	}

	@Override
	public void onResize() {
		// TODO: Is this really required??
		dock.onResize();
	}

}
