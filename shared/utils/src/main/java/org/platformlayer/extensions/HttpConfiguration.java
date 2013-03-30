package org.platformlayer.extensions;

import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.servlet.ServletModule.FilterKeyBindingBuilder;
import com.google.inject.servlet.ServletModule.ServletKeyBindingBuilder;

public interface HttpConfiguration {
	FilterKeyBindingBuilder filter(String urlPattern);

	ServletKeyBindingBuilder serve(String urlPattern);

	<T> AnnotatedBindingBuilder<T> bind(Class<T> clazz);
}
