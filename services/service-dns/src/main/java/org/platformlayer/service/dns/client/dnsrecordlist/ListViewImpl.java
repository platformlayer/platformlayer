package org.platformlayer.service.dns.client.dnsrecordlist;

import org.platformlayer.common.IsItem;
import org.platformlayer.gwt.client.ui.ListActivity;
import org.platformlayer.gwt.client.ui.ListView;
import org.platformlayer.gwt.client.ui.ViewHandler;
import org.platformlayer.ui.shared.client.views.AbstractApplicationView;

public abstract class ListViewImpl<T extends IsItem> extends AbstractApplicationView implements ListView<T> {

	protected ListActivity<?, ListView<T>, T> activity;

	@Override
	public void start(ViewHandler listener) {
		this.activity = (ListActivity<?, ListView<T>, T>) listener;
	}

}
