package org.platformlayer.ops.crypto;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

import javax.security.auth.x500.X500Principal;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.OpsException;

public interface ManagedSecretKey {
	X509Certificate[] getCertificateChain() throws OpsException;

	PrivateKey getPrivateKey() throws OpsException;

	boolean isCaKey();

	PlatformLayerKey createSignedKey(PlatformLayerKey parent, String keyId, X500Principal subject, KeyPair keyPair)
			throws OpsException;

	PublicKey getPublicKey() throws OpsException;
}
