package org.platformlayer.crypto;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.security.PrivateKey;

import org.bouncycastle.openssl.PEMReader;
import org.platformlayer.IoUtils;

public class PrivateKeys {
	public static PrivateKey fromPem(String data) {
		PEMReader reader = null;
		try {
			reader = new PEMReader(new StringReader(data), null, BouncyCastleLoader.getName());
			while (true) {
				Object o = reader.readObject();
				return (PrivateKey) o;
			}
		} catch (IOException e) {
			throw new IllegalArgumentException("Error parsing certificate", e);
		} finally {
			IoUtils.safeClose(reader);
		}
	}

	public static PrivateKey fromPem(File path) throws IOException {
		return fromPem(IoUtils.readAll(path));
	}
}
