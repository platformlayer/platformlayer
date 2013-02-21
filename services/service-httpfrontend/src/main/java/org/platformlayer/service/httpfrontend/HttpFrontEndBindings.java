package org.platformlayer.service.httpfrontend;

import org.platformlayer.ops.http.HttpManager;
import org.platformlayer.service.httpfrontend.ops.HttpFrontendManager;
import org.platformlayer.xaas.Module;

import com.google.inject.AbstractModule;

@Module
public class HttpFrontEndBindings extends AbstractModule {

	@Override
	protected void configure() {
		bind(HttpManager.class).to(HttpFrontendManager.class).asEagerSingleton();
	}

}
