package org.platformlayer.xaas.discovery;

import java.lang.annotation.Annotation;

public class AnnotatedClass {
	final Class<?> clazz;
	final Annotation annotation;

	public AnnotatedClass(Class<?> clazz, Annotation annotation) {
		this.clazz = clazz;
		this.annotation = annotation;
	}

	public Class<?> getSubjectClass() {
		return clazz;
	}

}