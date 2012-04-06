package org.platformlayer.ui.web.server;

import com.google.web.bindery.requestfactory.shared.ServiceLocator;

/**
 * This class provides an example of implementing a ServiceLocator to allow RequestFactory to work with instances of service objects, instead of its default behavior of mapping service calls to static
 * methods.
 * <p>
 * There is a reference to this class in an {@literal @}Service annotation in {@link com.google.gwt.sample.dynatablerf.shared.DynaTableRequestFactory}
 */
public class ServiceInfoServiceLocator implements ServiceLocator {

    public Object getInstance(Class<?> clazz) {
        return clazz.equals(ServiceInfoService.class) ? new ServiceInfoService() : null;
    }

}
