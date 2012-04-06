package org.platformlayer.ui.web.client.widgets;

import org.platformlayer.ui.web.shared.ServiceInfoProxy;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class ServiceWidget extends Composite {
    interface Binder extends UiBinder<Widget, ServiceWidget> {
    }

    interface Style extends CssResource {
    }

    final ServiceInfoProxy service;

    @UiField
    TabLayoutPanel tabs;

    public ServiceWidget(ServiceInfoProxy service) {
        this.service = service;
        initWidget(GWT.<Binder> create(Binder.class).createAndBindUi(this));

        // fetch();

    }

}
