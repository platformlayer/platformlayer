package org.platformlayer.auth.keystone;

import org.openstack.keystone.services.SystemAuthenticator;

import com.google.inject.AbstractModule;

public class KeystoneOpsSystemModule extends AbstractModule {

	@Override
	protected void configure() {
		KeystoneOpsUserModule userModule = new KeystoneOpsUserModule();
		install(userModule);

		bind(SystemAuthenticator.class).to(ClientCertificateSystemAuthenticator.class).asEagerSingleton();
		// bind(SystemAuthenticator.class).to(KeystoneSystemAuthenticator.class).asEagerSingleton();
	}
}
