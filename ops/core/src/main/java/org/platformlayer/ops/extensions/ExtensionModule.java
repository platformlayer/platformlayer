package org.platformlayer.ops.extensions;

import org.platformlayer.jdbc.simplejpa.ResultSetMappersProvider;

import com.google.inject.Module;

public interface ExtensionModule {
	void addEntities(ResultSetMappersProvider resultSetMappersProvider);

	void addFilters(HttpConfiguration webConfiguration);

	Module getOverrideModule();
}
