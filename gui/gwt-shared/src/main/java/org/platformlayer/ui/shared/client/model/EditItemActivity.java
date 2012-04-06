package org.platformlayer.ui.shared.client.model;
//package org.platformlayer.ui.web.client.service.dns;
//
//import org.platformlayer.ui.web.client.App;
//
//import com.google.gwt.activity.shared.AbstractActivity;
//import com.google.gwt.place.shared.Place;
//import com.google.gwt.user.client.ui.AcceptsOneWidget;
//
//public class EditItemActivity extends AbstractActivity implements HelloView.Presenter {
//    // Name that will be appended to "Hello,"
//    private String name;
//
//    public EditItemActivity(EditItemPlace place) {
//        this.name = place.getHelloName();
//    }
//
//    /**
//     * Invoked by the ActivityManager to start a new Activity
//     */
//    @Override
//    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
//        HelloView helloView = clientFactory.getHelloView();
//        helloView.setName(name);
//        helloView.setPresenter(this);
//        containerWidget.setWidget(helloView.asWidget());
//    }
//
//    // /**
//    // * Ask user before stopping this activity
//    // */
//    // @Override
//    // public String mayStop() {
//    // return "Please hold on. This activity is stopping.";
//    // }
//
//    /**
//     * Navigate to a new Place in the browser
//     */
//    public void goTo(Place place) {
//        App.injector.getPlaceController().goTo(place);
//    }
// }
