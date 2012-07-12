package org.platformlayer.ops.crypto;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import org.platformlayer.ops.OpsException;

public interface ManagedSecretKey {
	X509Certificate getCertificate() throws OpsException;

	PrivateKey getPrivateKey() throws OpsException;
}
