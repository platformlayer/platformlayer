package org.openstack.keystone.resources.admin;

import java.security.cert.X509Certificate;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.auth.AuthenticatorException;
import org.platformlayer.auth.ServiceAccount;
import org.platformlayer.auth.resources.PlatformlayerAuthResourceBase;
import org.platformlayer.auth.services.SystemAuthenticator;
import org.platformlayer.auth.services.TokenService;

public class RootResource extends PlatformlayerAuthResourceBase {
	static final Logger log = Logger.getLogger(RootResource.class);

	@Inject
	protected SystemAuthenticator systemAuthenticator;

	@Inject
	protected TokenService tokenService;

	protected void requireSystemAccess() throws AuthenticatorException {
		X509Certificate[] certChain = getCertificateChain();
		if (certChain != null && certChain.length != 0) {
			X509Certificate head = certChain[0];

			ServiceAccount auth = systemAuthenticator.authenticate(certChain);
			if (auth != null) {
				return;
			}

			log.debug("Certificate authentication request failed for " + head);
		}

		throwUnauthorized();

		// return myTokenInfo;
	}

}
