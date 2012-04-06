package org.platformlayer;

import java.lang.annotation.Annotation;

public class ClassInspection {
    final ClassLoader classLoader;

    public ClassInspection(ClassLoader classLoader) {
        super();
        this.classLoader = classLoader;
    }

    public Class<?> loadClass(String className) throws ClassNotFoundException {
        return classLoader.loadClass(className);
    }

    public Annotation findAnnotation(Class<?> clazz, String annotationName) {
        Annotation[] annotations = clazz.getAnnotations();
        for (Annotation annotation : annotations) {
            // getLog().info(annotation.annotationType().getName());
            if (annotation.annotationType().getName().equals(annotationName)) {
                return annotation;
            }
        }
        return null;
    }

}
