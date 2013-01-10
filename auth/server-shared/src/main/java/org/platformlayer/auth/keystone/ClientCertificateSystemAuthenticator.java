package org.platformlayer.auth.keystone;

import javax.inject.Inject;

import org.platformlayer.RepositoryException;
import org.platformlayer.auth.AuthenticatorException;
import org.platformlayer.auth.ServiceAccountEntity;
import org.platformlayer.auth.UserDatabase;
import org.platformlayer.auth.model.CertificateChainInfo;
import org.platformlayer.auth.model.CertificateInfo;
import org.platformlayer.auth.services.SystemAuthenticator;
import org.platformlayer.metrics.Instrumented;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.utils.Hex;
import com.google.common.base.Strings;

@Instrumented
public class ClientCertificateSystemAuthenticator implements SystemAuthenticator {
	private static final Logger log = LoggerFactory.getLogger(ClientCertificateSystemAuthenticator.class);

	@Inject
	UserDatabase repository;

	@Override
	public ServiceAccountEntity authenticate(CertificateChainInfo certChainInfo) throws AuthenticatorException {
		if (certChainInfo.certificates.size() == 0) {
			log.debug("Chain empty; can't authenticate");
			return null;
		}

		CertificateInfo head = certChainInfo.certificates.get(0);

		String subject = head.subjectDN;
		if (Strings.isNullOrEmpty(head.publicKey)) {
			throw new IllegalArgumentException();
		}
		byte[] publicKey = Hex.fromHex(head.publicKey);

		ServiceAccountEntity auth;
		try {
			auth = repository.findServiceAccount(subject, publicKey);
		} catch (RepositoryException e) {
			throw new AuthenticatorException("Error while authenticating user", e);
		}

		if (auth == null) {
			log.debug("Authentication failed - public key not recognized: " + Hex.toHex(publicKey));
		}

		return auth;
	}
}
