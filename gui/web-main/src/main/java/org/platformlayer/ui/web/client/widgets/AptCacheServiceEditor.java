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
//import org.platformlayer.ui.web.shared.AptCacheServiceProxy;
//
//import com.google.gwt.core.client.GWT;
//import com.google.gwt.editor.client.Editor;
//import com.google.gwt.editor.ui.client.ValueBoxEditorDecorator;
//import com.google.gwt.uibinder.client.UiBinder;
//import com.google.gwt.uibinder.client.UiField;
//import com.google.gwt.user.client.ui.Composite;
//import com.google.gwt.user.client.ui.Widget;
//
///**
// * Edits People.
// */
//public class AptCacheServiceEditor extends Composite implements Editor<AptCacheServiceProxy> {
//    interface Binder extends UiBinder<Widget, AptCacheServiceEditor> {
//    }
//
//    // @UiField
//    // AddressEditor address;
//
//    @UiField
//    ValueBoxEditorDecorator<String> dnsName;
//
//    // @UiField(provided = true)
//    // MentorSelector mentor;
//
//    // @UiField
//    // ValueBoxEditorDecorator<String> name;
//    //
//    // @UiField
//    // ValueBoxEditorDecorator<String> note;
//    //
//    // @UiField
//    // Focusable nameBox;
//
//    // @UiField(provided = true)
//    // ScheduleEditor classSchedule;
//
//    public AptCacheServiceEditor() {
//        initWidget(GWT.<Binder> create(Binder.class).createAndBindUi(this));
//    }
//
//    public void focus() {
//        // Scheduler.get().scheduleDeferred(new ScheduledCommand() {
//        // public void execute() {
//        // nameBox.setFocus(true);
//        // }
//        // });
//    }
// }
