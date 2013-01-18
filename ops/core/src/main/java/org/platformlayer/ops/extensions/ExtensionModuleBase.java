package org.platformlayer.ops.extensions;

import org.platformlayer.jdbc.simplejpa.ResultSetMappersProvider;

import com.google.inject.Module;

public class ExtensionModuleBase implements ExtensionModule {

	@Override
	public void addEntities(ResultSetMappersProvider resultSetMappersProvider) {

	}

	@Override
	public void addFilters(HttpConfiguration servletModule) {

	}

	@Override
	public Module getOverrideModule() {
		return null;
	}

}
