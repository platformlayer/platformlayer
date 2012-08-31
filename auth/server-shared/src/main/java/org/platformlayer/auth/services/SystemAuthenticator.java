package org.platformlayer.auth.services;

import org.platformlayer.auth.AuthenticatorException;
import org.platformlayer.auth.ServiceAccountEntity;
import org.platformlayer.auth.model.CertificateChainInfo;

public interface SystemAuthenticator {
	ServiceAccountEntity authenticate(CertificateChainInfo certChain) throws AuthenticatorException;
}
