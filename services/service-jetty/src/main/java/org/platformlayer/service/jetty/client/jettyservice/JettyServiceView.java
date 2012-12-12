package org.platformlayer.service.jetty.client.jettyservice;

import org.platformlayer.gwt.client.ui.ItemView;
import org.platformlayer.service.jetty.model.JettyService;

import com.google.inject.ImplementedBy;

@ImplementedBy(JettyServiceViewImpl.class)
public interface JettyServiceView extends ItemView<JettyService> {
}
