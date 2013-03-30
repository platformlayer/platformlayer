package org.platformlayer.extensions;

import java.util.List;

import org.platformlayer.jdbc.simplejpa.ResultSetMappersProvider;

import com.google.inject.Module;

public interface ExtensionModule {
	void addEntities(ResultSetMappersProvider resultSetMappersProvider);

	void addHttpExtensions(HttpConfiguration webConfiguration);

	Module getOverrideModule();

	List<Module> getExtraModules();
}
