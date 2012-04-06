package org.platformlayer.ui.web.client;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.platformlayer.ui.shared.client.model.ItemEditorWorkflow;
import org.platformlayer.ui.web.client.commons.ErrorDialog;
import org.platformlayer.ui.web.client.widgets.MainPanel;
import org.platformlayer.ui.web.shared.PlatformLayerRequestFactory;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryLogHandler;
import com.google.web.bindery.requestfactory.shared.LoggingRequest;

/**
 * The entry point class which performs the initial loading of the DynaTableRf application.
 */
public class PlatformLayerUi implements EntryPoint {
    interface Binder extends UiBinder<Widget, PlatformLayerUi> {
    }

    private static final Logger log = Logger.getLogger(PlatformLayerUi.class.getName());

    // @UiField(provided = true)
    // SummaryWidget calendar;

    // @UiField(provided = true)
    // AptCacheServiceGrid aptCacheServiceGrid;

    // EventBus eventBus = new SimpleEventBus();

    // @UiField(provided = true)
    // FavoritesWidget favorites;
    //
    // @UiField(provided = true)
    // DayFilterWidget filter;

    /**
     * This method sets up the top-level services used by the application.
     */
    public void onModuleLoad() {
        GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            public void onUncaughtException(Throwable e) {
                log.log(Level.SEVERE, e.getMessage(), e);
            }
        });

        // final PlatformLayerRequestFactory requests = GWT.create(PlatformLayerRequestFactory.class);

        // String baseUrl = GWT.getModuleBaseURL();
        // baseUrl = baseUrl.replace("/static/platformlayerui/", "/");
        //
        // String url = baseUrl + "gwtRequest";
        //
        // HttpRequestTransport transport = new HttpRequestTransport();
        // transport.setRequestUrl(url);
        //
        // Map<String, String> headers = transport.getHeaders();
        // headers.put("X-Auth-Token", "DEV-TOKEN-fathomdb");
        //
        // requests.initialize(eventBus, transport);

        final PlatformLayerRequestFactory requests = App.injector.getRequestFactory();

        // Add remote logging handler
        RequestFactoryLogHandler.LoggingRequestProvider provider = new RequestFactoryLogHandler.LoggingRequestProvider() {
            public LoggingRequest getLoggingRequest() {
                return requests.loggingRequest();
            }
        };
        Logger.getLogger("").addHandler(new ErrorDialog().getHandler());
        Logger.getLogger("").addHandler(new RequestFactoryLogHandler(provider, Level.WARNING, new ArrayList<String>()));
        // FavoritesManager manager = new FavoritesManager(requests);
        // PersonEditorWorkflow.register(eventBus, requests, manager);

        EventBus eventBus = App.injector.getEventBus();

        // AptCacheServiceEditorWorkflow.register(eventBus, requests);
        ItemEditorWorkflow.register(eventBus, requests);
        // calendar = new SummaryWidget(eventBus, requests, 15);
        // // favorites = new FavoritesWidget(eventBus, requests, manager);
        // // filter = new DayFilterWidget(eventBus);
        //
        // // TODO: Use Guice/Gin to avoid provided = true
        // aptCacheServiceGrid = new AptCacheServiceGrid(eventBus, requests, 15);

        MainPanel mainPanel = App.injector.getMainPanel();
        RootLayoutPanel.get().add(mainPanel);
        //
        // RootLayoutPanel.get().add(GWT.<Binder> create(Binder.class).createAndBindUi(this));

        // Fast test to see if the sample is not being run from devmode
        if (GWT.getHostPageBaseURL().startsWith("file:")) {
            log.log(Level.SEVERE, "The DynaTableRf sample cannot be run without its" + " server component.  If you are running the sample from a"
                    + " GWT distribution, use the 'ant devmode' target to launch" + " the DTRF server.");
        }
    }
}
