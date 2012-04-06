package org.platformlayer.core.model;

import javax.crypto.SecretKey;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

/**
 * This class is used to hold / pass the encryption keys. Currently all the fields are transient.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class SecretInfo {
    public SecretInfo() {
    }

    public SecretInfo(byte[] data) {
        this.data = data;
    }

    @XmlTransient
    private byte[] data;

    @XmlTransient
    private SecretKey secret;

    public SecretKey getSecret() {
        if (secret == null)
            throw new IllegalStateException();
        return secret;
    }

    public boolean isLocked() {
        return secret == null;
    }

    public void unlock(SecretKey itemSecret) {
        this.secret = itemSecret;
    }

    public byte[] getEncoded() {
        return data;
    }
}
