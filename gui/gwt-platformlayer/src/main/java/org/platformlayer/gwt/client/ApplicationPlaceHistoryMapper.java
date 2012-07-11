package org.platformlayer.gwt.client;

import org.platformlayer.gwt.client.home.HomePlace;
import org.platformlayer.gwt.client.login.LoginPlace;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;

@WithTokenizers({ HomePlace.Tokenizer.class, LoginPlace.Tokenizer.class })
public interface ApplicationPlaceHistoryMapper extends PlaceHistoryMapper {
}