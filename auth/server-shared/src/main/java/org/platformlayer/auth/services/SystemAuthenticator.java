package org.platformlayer.auth.services;

import java.security.cert.X509Certificate;

import org.platformlayer.auth.AuthenticatorException;
import org.platformlayer.auth.ServiceAccount;

public interface SystemAuthenticator {
	ServiceAccount authenticate(X509Certificate[] certChain) throws AuthenticatorException;
}
