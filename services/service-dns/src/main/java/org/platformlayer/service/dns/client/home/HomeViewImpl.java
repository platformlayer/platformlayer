package org.platformlayer.service.dns.client.home;

import static com.google.gwt.query.client.GQuery.$;

import javax.inject.Singleton;

import org.platformlayer.gwt.client.ui.ViewHandler;
import org.platformlayer.ui.shared.client.views.AbstractApplicationView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.query.client.Function;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HTMLPanel;

@Singleton
public class HomeViewImpl extends AbstractApplicationView implements HomeView {

	interface ViewUiBinder extends UiBinder<HTMLPanel, HomeViewImpl> {
	}

	private static ViewUiBinder viewUiBinder = GWT.create(ViewUiBinder.class);

	public HomeViewImpl() {
		initWidget(viewUiBinder.createAndBindUi(this));

		Element el = getElement();
		$(el).find("[place]").click(new Function() {
			@Override
			public boolean f(Event e) {
				String place = $(e).attr("place");
				HomePlace homePlace = activity.getPlace();
				activity.goTo(homePlace.getChild(place));
				return true;
			}
		});
	}

	private HomeActivity activity;

	@Override
	public void start(ViewHandler activity) {
		this.activity = (HomeActivity) activity;

		// alerts.clear();
	}

}