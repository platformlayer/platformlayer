package org.platformlayer.extensions;

import java.util.List;

import com.fathomdb.Configuration;
import com.fathomdb.jpa.impl.ResultSetMappersProvider;
import com.google.inject.Module;

public interface ExtensionModule {
	void addEntities(ResultSetMappersProvider resultSetMappersProvider);

	void addHttpExtensions(HttpConfiguration webConfiguration);

	Module getOverrideModule();

	List<Module> getExtraModules(Configuration configuration);
}
