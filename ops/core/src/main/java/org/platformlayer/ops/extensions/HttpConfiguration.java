package org.platformlayer.ops.extensions;

import com.google.inject.servlet.ServletModule.FilterKeyBindingBuilder;

public interface HttpConfiguration {
	FilterKeyBindingBuilder filter(String urlPattern);
}
