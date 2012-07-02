package org.openstack.keystone.services;

import java.security.cert.X509Certificate;

public interface SystemAuthenticator {
	ServiceAccount authenticate(X509Certificate[] certChain) throws AuthenticatorException;
}
