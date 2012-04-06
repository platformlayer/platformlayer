//package org.platformlayer.ui.web.client.places;
//
//import com.google.gwt.place.shared.Place;
//import com.google.gwt.place.shared.PlaceTokenizer;
//
//public class EditItemPlace extends Place {
//    private String helloName;
//
//    public EditItemPlace(String token) {
//        this.helloName = token;
//    }
//
//    public String getHelloName() {
//        return helloName;
//    }
//
//    public static class Tokenizer implements PlaceTokenizer<EditItemPlace> {
//        @Override
//        public String getToken(EditItemPlace place) {
//            return place.getHelloName();
//        }
//
//        @Override
//        public EditItemPlace getPlace(String token) {
//            return new EditItemPlace(token);
//        }
//    }
// }
