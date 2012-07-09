package org.platformlayer.ops.crypto;

import org.openstack.crypto.CertificateAndKey;
import org.platformlayer.ops.OpsException;

public interface EncryptionStore {
	CertificateAndKey getCertificateAndKey(String cert) throws OpsException;
}
