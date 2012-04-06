package org.platformlayer.ui.web.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class MainPanel extends Composite {

    interface Binder extends UiBinder<Widget, MainPanel> {
    }

    public MainPanel() {
        initWidget(GWT.<Binder> create(Binder.class).createAndBindUi(this));
    }

}
