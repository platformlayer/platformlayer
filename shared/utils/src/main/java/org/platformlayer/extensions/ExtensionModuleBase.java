package org.platformlayer.extensions;

import java.util.Collections;
import java.util.List;

import org.platformlayer.jdbc.simplejpa.ResultSetMappersProvider;

import com.google.inject.Module;

public class ExtensionModuleBase implements ExtensionModule {

	@Override
	public void addEntities(ResultSetMappersProvider resultSetMappersProvider) {

	}

	@Override
	public void addHttpExtensions(HttpConfiguration servletModule) {

	}

	@Override
	public Module getOverrideModule() {
		return null;
	}

	@Override
	public List<Module> getExtraModules() {
		return Collections.emptyList();
	}

}
