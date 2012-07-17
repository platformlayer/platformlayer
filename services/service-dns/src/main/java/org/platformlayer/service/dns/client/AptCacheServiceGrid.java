//import java.util.Collections;
//import java.util.List;
//
//import org.platformlayer.ui.shared.client.model.ItemGrid.TableResources;
//
//import com.google.gwt.cell.client.TextCell;
//import com.google.gwt.core.client.GWT;
//import com.google.gwt.event.dom.client.ClickEvent;
//import com.google.gwt.event.shared.EventBus;
//import com.google.gwt.uibinder.client.UiBinder;
//import com.google.gwt.uibinder.client.UiField;
//import com.google.gwt.uibinder.client.UiHandler;
//import com.google.gwt.user.cellview.client.Column;
//import com.google.gwt.user.cellview.client.DataGrid;
//import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
//import com.google.gwt.user.cellview.client.SimplePager;
//import com.google.gwt.user.client.ui.DockLayoutPanel;
//import com.google.gwt.user.client.ui.Widget;
//import com.google.gwt.view.client.Range;
//import com.google.gwt.view.client.RangeChangeEvent;
//import com.google.gwt.view.client.SelectionChangeEvent;
//import com.google.web.bindery.requestfactory.shared.EntityProxyChange;
//import com.google.web.bindery.requestfactory.shared.EntityProxyId;
//import com.google.web.bindery.requestfactory.shared.Receiver;
//import com.google.web.bindery.requestfactory.shared.WriteOperation;
//
///**
// * A paging table with summaries of all known people.
// */
//public class AptCacheServiceGrid extends GridBase<AptCacheServiceProxy> {
//
//	interface Binder extends UiBinder<Widget, AptCacheServiceGrid> {
//	}
//
//	private class DescriptionColumn extends Column<AptCacheServiceProxy, String> {
//		public DescriptionColumn() {
//			super(new TextCell());
//		}
//
//		@Override
//		public String getValue(AptCacheServiceProxy object) {
//			return object.getDnsName();
//		}
//	}
//
//	@UiField
//	DockLayoutPanel dock;
//
//	@UiField(provided = true)
//	SimplePager pager = new SimplePager();
//
//	@UiField(provided = true)
//	DataGrid<AptCacheServiceProxy> table;
//
//	@Override
//	protected DataGrid<AptCacheServiceProxy> getTable() {
//		// TODO: Can UiFields be inherited??
//		return table;
//	}
//
//	private final EventBus eventBus;
//	private int lastFetch;
//	private final int numRows;
//	private final PlatformLayerRequestFactory requestFactory;
//
//	public AptCacheServiceGrid(EventBus eventBus, PlatformLayerRequestFactory requestFactory, int numRows) {
//		this.eventBus = eventBus;
//		this.requestFactory = requestFactory;
//		this.numRows = numRows;
//
//		table = new DataGrid<AptCacheServiceProxy>(numRows, GWT.<TableResources> create(TableResources.class));
//		initWidget(GWT.<Binder> create(Binder.class).createAndBindUi(this));
//
//		// Column<PersonProxy, String> nameColumn = new NameColumn();
//		// table.addColumn(nameColumn, "Name");
//		// table.setColumnWidth(nameColumn, "25ex");
//		Column<AptCacheServiceProxy, String> descriptionColumn = new DescriptionColumn();
//		table.addColumn(descriptionColumn, "Description");
//		table.setColumnWidth(descriptionColumn, "40ex");
//		// table.addColumn(new ScheduleColumn(), "Schedule");
//		table.setRowCount(numRows, false);
//		table.setSelectionModel(selectionModel);
//		table.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
//
//		EntityProxyChange.registerForProxyType(eventBus, AptCacheServiceProxy.class,
//				new EntityProxyChange.Handler<AptCacheServiceProxy>() {
//					@Override
//					public void onProxyChange(EntityProxyChange<AptCacheServiceProxy> event) {
//						AptCacheServiceGrid.this.onPersonChanged(event);
//					}
//				});
//
//		// FilterChangeEvent.register(eventBus, new FilterChangeEvent.Handler() {
//		// @Override
//		// public void onFilterChanged(FilterChangeEvent e) {
//		// filter.set(e.getDay(), e.isSelected());
//		// if (!pending) {
//		// pending = true;
//		// Scheduler.get().scheduleFinally(new ScheduledCommand() {
//		// @Override
//		// public void execute() {
//		// pending = false;
//		// fetch(0);
//		// }
//		// });
//		// }
//		// }
//		// });
//
//		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
//			@Override
//			public void onSelectionChange(SelectionChangeEvent event) {
//				AptCacheServiceGrid.this.refreshSelection();
//			}
//		});
//
//		fetch(0);
//	}
//
//	@UiHandler("create")
//	void onCreate(ClickEvent event) {
//		AptCacheServiceRequest context = requestFactory.aptCacheRequest();
//		// AptCacheServiceProxy aptCache = context.create(AptCacheServiceProxy.class);
//
//		// AddressProxy address = context.create(AddressProxy.class);
//		// ScheduleProxy schedule = context.create(ScheduleProxy.class);
//		// schedule.setTimeSlots(new ArrayList<TimeSlotProxy>());
//		AptCacheServiceProxy aptCache = context.edit(context.create(AptCacheServiceProxy.class));
//		// person.setAddress(address);
//		// person.setClassSchedule(schedule);
//		context.persist(aptCache);
//		eventBus.fireEvent(new EditAptCacheServiceEvent(aptCache, context));
//	}
//
//	void onPersonChanged(EntityProxyChange<AptCacheServiceProxy> event) {
//		if (WriteOperation.PERSIST.equals(event.getWriteOperation())) {
//			// Re-fetch if we're already displaying the last page
//			if (table.isRowCountExact()) {
//				fetch(lastFetch + 1);
//			}
//		}
//		if (WriteOperation.UPDATE.equals(event.getWriteOperation())) {
//			EntityProxyId<AptCacheServiceProxy> personId = event.getProxyId();
//
//			// Is the changing record onscreen?
//			int displayOffset = offsetOf(personId);
//			if (displayOffset != -1) {
//				// Record is onscreen and may differ from our data
//				requestFactory.find(personId).fire(new Receiver<AptCacheServiceProxy>() {
//					@Override
//					public void onSuccess(AptCacheServiceProxy person) {
//						// Re-check offset in case of changes while waiting for data
//						int offset = offsetOf(person.stableId());
//						if (offset != -1) {
//							table.setRowData(table.getPageStart() + offset, Collections.singletonList(person));
//						}
//					}
//				});
//			}
//		}
//	}
//
//	@UiHandler("table")
//	void onRangeChange(RangeChangeEvent event) {
//		Range r = event.getNewRange();
//		int start = r.getStart();
//
//		fetch(start);
//	}
//
//	private void fetch(final int start) {
//		lastFetch = start;
//		requestFactory.aptCacheRequest().findAll(/* start, numRows, filter */)
//				.fire(new Receiver<List<AptCacheServiceProxy>>() {
//					@Override
//					public void onSuccess(List<AptCacheServiceProxy> response) {
//						if (lastFetch != start) {
//							return;
//						}
//
//						int responses = response.size();
//						table.setRowData(start, response);
//						pager.setPageStart(start);
//						if (start == 0 || !table.isRowCountExact()) {
//							table.setRowCount(start + responses, responses < numRows);
//						}
//					}
//				});
//	}
//
// }
