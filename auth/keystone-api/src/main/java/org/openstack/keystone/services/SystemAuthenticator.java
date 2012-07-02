package org.openstack.keystone.services;

import java.security.cert.X509Certificate;

public interface SystemAuthenticator {
	SystemAuth authenticate(X509Certificate[] certChain);
}
