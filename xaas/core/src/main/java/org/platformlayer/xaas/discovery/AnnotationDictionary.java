package org.platformlayer.xaas.discovery;

import java.lang.annotation.Annotation;
import java.util.List;

import com.google.common.collect.Lists;

public class AnnotationDictionary {
    final Class<? extends Annotation> annotationClass;

    final List<AnnotatedClass> classes = Lists.newArrayList();

    public AnnotationDictionary(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    public void add(Class<?> clazz) {
        Annotation annotation = clazz.getAnnotation(annotationClass);
        if (annotation == null) {
            throw new IllegalArgumentException();
        }

        classes.add(new AnnotatedClass(clazz, annotation));
    }

    public List<AnnotatedClass> getAll() {
        return classes;
    }

}
