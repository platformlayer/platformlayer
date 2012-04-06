package org.platformlayer.ops.helpers;

import java.security.KeyPair;

public class SshKey {
    final String name;
    final String user;
    final KeyPair keyPair;

    public SshKey(String name, String user, KeyPair keyPair) {
        this.name = name;
        this.user = user;
        this.keyPair = keyPair;
    }

    public String getName() {
        return name;
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    public String getUser() {
        return user;
    }

}
