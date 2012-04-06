package org.platformlayer.xaas.discovery;

import java.lang.annotation.Annotation;
import java.util.List;

public interface AnnotationDiscovery {
    List<AnnotatedClass> findAnnotatedClasses(Class<? extends Annotation> annotation);
}
