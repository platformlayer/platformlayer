//package org.platformlayer.ui.web.client.places;
//
//import org.platformlayer.ui.web.client.service.dns.EditItemActivity;
//
//import com.google.gwt.activity.shared.Activity;
//import com.google.gwt.activity.shared.ActivityMapper;
//import com.google.gwt.place.shared.Place;
//
//public class AppActivityMapper implements ActivityMapper {
//
//    public AppActivityMapper() {
//    }
//
//    @Override
//    public Activity getActivity(Place place) {
//        if (place instanceof EditItemPlace)
//            return new EditItemActivity((EditItemPlace) place);
//        // else if (place instanceof GoodbyePlace)
//        // return new GoodbyeActivity((GoodbyePlace) place, clientFactory);
//        return null;
//    }
// }