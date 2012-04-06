package org.platformlayer.ops.crypto;

import java.security.PrivateKey;
import java.security.PublicKey;

public interface OpsKeyStore {

    public PrivateKey findPrivateKey(int keyId);

    public PublicKey findPublicKey(int backendId);

    public Iterable<Integer> getBackends();

    // public Iterable<Integer> getProjectIds();

    // public SecretKey findUserSecret(int userId);
}
