package org.platformlayer.ssh.mina.ciphers;

import org.apache.sshd.common.Cipher;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.cipher.BaseCipher;

public class AES128CTR extends BaseCipher {

    public static class Factory implements NamedFactory<Cipher> {
        @Override
        public String getName() {
            return "aes128-ctr";
        }

        @Override
        public Cipher create() {
            return new AES128CTR();
        }
    }

    public AES128CTR() {
        super(16, 16, "AES", "AES/CTR/NoPadding");
    }

}