///*
// * Copyright 2010 Google Inc.
// * 
// * Licensed under the Apache License, Version 2.0 (the "License"); you may not
// * use this file except in compliance with the License. You may obtain a copy of
// * the License at
// * 
// * http://www.apache.org/licenses/LICENSE-2.0
// * 
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
// * License for the specific language governing permissions and limitations under
// * the License.
// */
//package org.platformlayer.ui.web.client.widgets;
//
//import java.util.Collections;
//import java.util.List;
//
//import org.platformlayer.ui.web.shared.PlatformLayerRequestFactory;
//import org.platformlayer.ui.web.shared.PlatformLayerRequestFactory.ServiceInfoRequest;
//import org.platformlayer.ui.web.shared.ServiceInfoProxy;
//
//import com.google.gwt.cell.client.TextCell;
//import com.google.gwt.core.client.GWT;
//import com.google.gwt.event.dom.client.ClickEvent;
//import com.google.gwt.event.shared.EventBus;
//import com.google.gwt.resources.client.CssResource;
//import com.google.gwt.uibinder.client.UiBinder;
//import com.google.gwt.uibinder.client.UiField;
//import com.google.gwt.uibinder.client.UiHandler;
//import com.google.gwt.user.cellview.client.Column;
//import com.google.gwt.user.cellview.client.DataGrid;
//import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
//import com.google.gwt.user.cellview.client.SimplePager;
//import com.google.gwt.user.client.ui.Composite;
//import com.google.gwt.user.client.ui.DockLayoutPanel;
//import com.google.gwt.user.client.ui.Widget;
//import com.google.gwt.view.client.Range;
//import com.google.gwt.view.client.RangeChangeEvent;
//import com.google.gwt.view.client.SelectionChangeEvent;
//import com.google.gwt.view.client.SingleSelectionModel;
//import com.google.web.bindery.requestfactory.shared.EntityProxyChange;
//import com.google.web.bindery.requestfactory.shared.EntityProxyId;
//import com.google.web.bindery.requestfactory.shared.Receiver;
//import com.google.web.bindery.requestfactory.shared.WriteOperation;
//
///**
// * A paging table with summaries of all known people.
// */
//public class SummaryWidget extends Composite {
//
//    interface Binder extends UiBinder<Widget, SummaryWidget> {
//    }
//
//    interface Style extends CssResource {
//    }
//
//    interface TableResources extends DataGrid.Resources {
//        @Override
//        @Source(value = { DataGrid.Style.DEFAULT_CSS, "DataGridPatch.css" })
//        DataGrid.Style dataGridStyle();
//    }
//
//    private class DescriptionColumn extends Column<ServiceInfoProxy, String> {
//        public DescriptionColumn() {
//            super(new TextCell());
//        }
//
//        @Override
//        public String getValue(ServiceInfoProxy object) {
//            return object.getDescription();
//        }
//    }
//
//    // private class NameColumn extends Column<PersonProxy, String> {
//    // public NameColumn() {
//    // super(new TextCell());
//    // }
//    //
//    // @Override
//    // public String getValue(PersonProxy object) {
//    // return object.getName();
//    // }
//    // }
//    //
//    // private class ScheduleColumn extends Column<PersonProxy, String> {
//    // public ScheduleColumn() {
//    // super(new TextCell());
//    // }
//    //
//    // @Override
//    // public String getValue(PersonProxy object) {
//    // return object.getScheduleDescription();
//    // }
//    // }
//
//    @UiField
//    DockLayoutPanel dock;
//
//    @UiField(provided = true)
//    SimplePager pager = new SimplePager();
//
//    @UiField(provided = true)
//    DataGrid<ServiceInfoProxy> table;
//
//    private final EventBus eventBus;
//    // private List<Boolean> filter = new ArrayList<Boolean>(ALL_DAYS);
//    private int lastFetch;
//    private final int numRows;
//    private boolean pending;
//    private final PlatformLayerRequestFactory requestFactory;
//    private final SingleSelectionModel<ServiceInfoProxy> selectionModel = new SingleSelectionModel<ServiceInfoProxy>();
//
//    public SummaryWidget(EventBus eventBus, PlatformLayerRequestFactory requestFactory, int numRows) {
//        this.eventBus = eventBus;
//        this.requestFactory = requestFactory;
//        this.numRows = numRows;
//
//        table = new DataGrid<ServiceInfoProxy>(numRows, GWT.<TableResources> create(TableResources.class));
//        initWidget(GWT.<Binder> create(Binder.class).createAndBindUi(this));
//
//        // Column<PersonProxy, String> nameColumn = new NameColumn();
//        // table.addColumn(nameColumn, "Name");
//        // table.setColumnWidth(nameColumn, "25ex");
//        Column<ServiceInfoProxy, String> descriptionColumn = new DescriptionColumn();
//        table.addColumn(descriptionColumn, "Description");
//        table.setColumnWidth(descriptionColumn, "40ex");
//        // table.addColumn(new ScheduleColumn(), "Schedule");
//        table.setRowCount(numRows, false);
//        table.setSelectionModel(selectionModel);
//        table.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
//
//        EntityProxyChange.registerForProxyType(eventBus, ServiceInfoProxy.class, new EntityProxyChange.Handler<ServiceInfoProxy>() {
//            @Override
//            public void onProxyChange(EntityProxyChange<ServiceInfoProxy> event) {
//                SummaryWidget.this.onPersonChanged(event);
//            }
//        });
//
//        // FilterChangeEvent.register(eventBus, new FilterChangeEvent.Handler() {
//        // @Override
//        // public void onFilterChanged(FilterChangeEvent e) {
//        // filter.set(e.getDay(), e.isSelected());
//        // if (!pending) {
//        // pending = true;
//        // Scheduler.get().scheduleFinally(new ScheduledCommand() {
//        // @Override
//        // public void execute() {
//        // pending = false;
//        // fetch(0);
//        // }
//        // });
//        // }
//        // }
//        // });
//
//        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
//            @Override
//            public void onSelectionChange(SelectionChangeEvent event) {
//                SummaryWidget.this.refreshSelection();
//            }
//        });
//
//        fetch(0);
//    }
//
//    @UiHandler("create")
//    void onCreate(ClickEvent event) {
//        ServiceInfoRequest context = requestFactory.serviceInfoRequest();
//        // AddressProxy address = context.create(AddressProxy.class);
//        // ScheduleProxy schedule = context.create(ScheduleProxy.class);
//        // schedule.setTimeSlots(new ArrayList<TimeSlotProxy>());
//        ServiceInfoProxy person = context.edit(context.create(ServiceInfoProxy.class));
//        // person.setAddress(address);
//        // person.setClassSchedule(schedule);
//        // context.persist().using(person);
//        // eventBus.fireEvent(new EditPersonEvent(person, context));
//    }
//
//    void onPersonChanged(EntityProxyChange<ServiceInfoProxy> event) {
//        if (WriteOperation.PERSIST.equals(event.getWriteOperation())) {
//            // Re-fetch if we're already displaying the last page
//            if (table.isRowCountExact()) {
//                fetch(lastFetch + 1);
//            }
//        }
//        if (WriteOperation.UPDATE.equals(event.getWriteOperation())) {
//            EntityProxyId<ServiceInfoProxy> personId = event.getProxyId();
//
//            // Is the changing record onscreen?
//            int displayOffset = offsetOf(personId);
//            if (displayOffset != -1) {
//                // Record is onscreen and may differ from our data
//                requestFactory.find(personId).fire(new Receiver<ServiceInfoProxy>() {
//                    @Override
//                    public void onSuccess(ServiceInfoProxy person) {
//                        // Re-check offset in case of changes while waiting for data
//                        int offset = offsetOf(person.stableId());
//                        if (offset != -1) {
//                            table.setRowData(table.getPageStart() + offset, Collections.singletonList(person));
//                        }
//                    }
//                });
//            }
//        }
//    }
//
//    @UiHandler("table")
//    void onRangeChange(RangeChangeEvent event) {
//        Range r = event.getNewRange();
//        int start = r.getStart();
//
//        fetch(start);
//    }
//
//    void refreshSelection() {
//        ServiceInfoProxy person = selectionModel.getSelectedObject();
//        if (person == null) {
//            return;
//        }
//        // eventBus.fireEvent(new EditPersonEvent(person));
//        selectionModel.setSelected(person, false);
//    }
//
//    private void fetch(final int start) {
//        lastFetch = start;
//        requestFactory.serviceInfoRequest().findAll(/* start, numRows, filter */).fire(new Receiver<List<ServiceInfoProxy>>() {
//            @Override
//            public void onSuccess(List<ServiceInfoProxy> response) {
//                if (lastFetch != start) {
//                    return;
//                }
//
//                int responses = response.size();
//                table.setRowData(start, response);
//                pager.setPageStart(start);
//                if (start == 0 || !table.isRowCountExact()) {
//                    table.setRowCount(start + responses, responses < numRows);
//                }
//            }
//        });
//    }
//
//    private int offsetOf(EntityProxyId<ServiceInfoProxy> personId) {
//        List<ServiceInfoProxy> displayedItems = table.getVisibleItems();
//        for (int offset = 0, j = displayedItems.size(); offset < j; offset++) {
//            if (personId.equals(displayedItems.get(offset).stableId())) {
//                return offset;
//            }
//        }
//        return -1;
//    }
// }
