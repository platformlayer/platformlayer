package org.platformlayer.xaas.services;

import java.util.List;

import javax.inject.Inject;

import org.platformlayer.xaas.Service;
import org.platformlayer.xaas.discovery.AnnotatedClass;
import org.platformlayer.xaas.discovery.AnnotationDiscovery;

import com.google.inject.Injector;

public class AnnotationServiceProviderDictionary extends ServiceProviderDirectoryBase {
    final AnnotationDiscovery discovery;

    @Inject
    public AnnotationServiceProviderDictionary(Injector injector, AnnotationDiscovery discovery) {
        super(injector);

        this.discovery = discovery;

        List<AnnotatedClass> annotatedClasses = discovery.findAnnotatedClasses(Service.class);

        for (AnnotatedClass annotatedClass : annotatedClasses) {
            addService(annotatedClass.getSubjectClass());
        }
    }

}
