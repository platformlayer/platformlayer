package org.platformlayer.service.aptcache.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class ServicePanel extends Composite {
    interface Binder extends UiBinder<Widget, ServicePanel> {
    }

    interface Style extends CssResource {
    }

    @UiField
    TabLayoutPanel tabs;

    public ServicePanel() {
        initWidget(GWT.<Binder> create(Binder.class).createAndBindUi(this));

        tabs.selectTab(0);
    }

    @UiHandler("tabs")
    public void onClick(SelectionEvent<Integer> event) {
        // TODO: Is this really required??
        Widget widget = tabs.getWidget(event.getSelectedItem());
        if (widget instanceof RequiresResize) {
            ((RequiresResize) widget).onResize();
        }
    }
}
