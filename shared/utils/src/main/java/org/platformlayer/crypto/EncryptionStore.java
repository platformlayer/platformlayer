package org.platformlayer.crypto;

import org.platformlayer.ops.OpsException;

import com.fathomdb.crypto.CertificateAndKey;

public interface EncryptionStore {
	CertificateAndKey getCertificateAndKey(String cert) throws OpsException;
}
