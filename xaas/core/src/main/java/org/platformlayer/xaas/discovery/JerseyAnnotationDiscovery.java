package org.platformlayer.xaas.discovery;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import org.platformlayer.xaas.Controller;
import org.platformlayer.xaas.Service;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.sun.jersey.core.spi.scanning.FilesScanner;
import com.sun.jersey.core.spi.scanning.Scanner;
import com.sun.jersey.spi.scanning.AnnotationScannerListener;

public class JerseyAnnotationDiscovery implements AnnotationDiscovery {
    static Class<? extends Annotation>[] ANNOTATIONS = (Class<? extends Annotation>[]) new Class<?>[] { Service.class, Controller.class };

    final Map<Class<? extends Annotation>, AnnotationDictionary> annotationDictionaries = new MapMaker().makeComputingMap(new Function<Class<? extends Annotation>, AnnotationDictionary>() {
        @Override
        public AnnotationDictionary apply(Class<? extends Annotation> input) {
            return new AnnotationDictionary(input);
        }
    });

    public void scan() {
        Scanner scanner = buildScanner();

        AnnotationScannerListener listener = new AnnotationScannerListener(ANNOTATIONS);
        scanner.scan(listener);

        for (Class<?> clazz : listener.getAnnotatedClasses()) {
            for (Class<? extends Annotation> annotation : ANNOTATIONS) {
                if (clazz.isAnnotationPresent(annotation)) {
                    AnnotationDictionary annotationDictionary = getAnnotationDictionary(annotation);
                    annotationDictionary.add(clazz);
                }
            }
        }
    }

    private Scanner buildScanner() {
        List<File> paths = Lists.newArrayList();
        String classpath = System.getProperty("java.class.path");
        for (String classpathEntry : classpath.split(File.pathSeparator)) {
            paths.add(new File(classpathEntry));
        }
        Scanner scanner = new FilesScanner(paths.toArray(new File[paths.size()]));
        return scanner;
    }

    private AnnotationDictionary getAnnotationDictionary(Class<? extends Annotation> annotation) {
        return annotationDictionaries.get(annotation);
    }

    @Override
    public List<AnnotatedClass> findAnnotatedClasses(Class<? extends Annotation> annotation) {
        return annotationDictionaries.get(annotation).getAll();
    }
}