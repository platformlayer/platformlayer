package org.openstack.keystone.resources.admin;

import java.security.cert.X509Certificate;

import javax.inject.Inject;

import org.platformlayer.auth.AuthenticatorException;
import org.platformlayer.auth.ServiceAccount;
import org.platformlayer.auth.model.CertificateChainInfo;
import org.platformlayer.auth.model.CertificateInfo;
import org.platformlayer.auth.resources.PlatformlayerAuthResourceBase;
import org.platformlayer.auth.services.SystemAuthenticator;
import org.platformlayer.auth.services.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.utils.Hex;

public class RootResource extends PlatformlayerAuthResourceBase {
	private static final Logger log = LoggerFactory.getLogger(RootResource.class);

	@Inject
	protected SystemAuthenticator systemAuthenticator;

	@Inject
	protected TokenService tokenService;

	protected void requireSystemAccess() throws AuthenticatorException {
		X509Certificate[] certChain = getCertificateChain();
		if (certChain != null && certChain.length != 0) {
			CertificateChainInfo chain = new CertificateChainInfo();
			for (X509Certificate cert : certChain) {
				CertificateInfo info = new CertificateInfo();
				info.publicKey = Hex.toHex(cert.getPublicKey().getEncoded());
				info.subjectDN = cert.getSubjectDN().getName();
				chain.certificates.add(info);
			}

			ServiceAccount auth = systemAuthenticator.authenticate(chain);
			if (auth != null) {
				return;
			}

			log.debug("Certificate authentication request failed for " + chain);
		} else {
			log.debug("Certificate authentication failed (no certificate presented)");
		}

		throwUnauthorized();

		// return myTokenInfo;
	}

}
