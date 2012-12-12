package org.platformlayer.service.jetty.client.jettyservice;

import java.util.logging.Logger;

import org.platformlayer.gwt.client.ui.ItemActivity;
import org.platformlayer.service.jetty.client.JettyPlugin;
import org.platformlayer.service.jetty.model.JettyService;

import com.google.inject.Inject;

public class JettyServiceActivity extends ItemActivity<JettyServicePlace, JettyServiceView, JettyService> {
	static final Logger log = Logger.getLogger(JettyServiceActivity.class.getName());

	public JettyServiceActivity() {
		super(JettyPlugin.SERVICE_TYPE, JettyPlugin.ITEM_TYPE_JETTYSERVICE);
	}

	@Inject
	JettyServiceView view;

	@Override
	protected JettyServiceView getView() {
		return view;
	}
}