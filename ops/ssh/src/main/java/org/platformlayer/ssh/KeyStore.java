package org.platformlayer.ssh;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.util.Map;

import org.bouncycastle.openssl.PEMReader;
import org.openstack.utils.Io;
import org.openstack.utils.Utf8;
import org.platformlayer.IoUtils;

import com.google.common.collect.Maps;

public class KeyStore {
    static Map<String, KeyPair> keys = Maps.newHashMap();

    public static KeyPair loadKeyPair(String path) throws IOException {
        File file = Io.resolve(path);
        return loadKeyPair(file);
    }

    public static synchronized KeyPair loadKeyPair(File file) throws IOException {
        String cacheKey = file.getAbsolutePath();
        KeyPair keyPair = keys.get(cacheKey);
        if (keyPair == null) {
            PEMReader pemReader = null;
            try {
                pemReader = new PEMReader(Utf8.openFile(file));
                Object o = pemReader.readObject();
                keyPair = (KeyPair) o;
            } finally {
                IoUtils.safeClose(pemReader);
            }

            keys.put(cacheKey, keyPair);
        }

        return keyPair;
    }
}
