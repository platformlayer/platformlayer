package org.platformlayer.ui.shared.shared;

import java.util.List;

import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.SkipInterfaceValidation;

@SkipInterfaceValidation
public interface BaseEntityRequest<T> extends RequestContext {
	Request<List<T>> findAll();

	// This doesn't work - I think it's http://code.google.com/p/google-web-toolkit/issues/detail?id=6794
	// Request<T> persist(T proxy);
}
