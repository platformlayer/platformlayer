package org.platformlayer.crypto;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.PrivateKey;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;

import com.fathomdb.io.IoUtils;
import com.google.common.io.Closeables;

public class PrivateKeys {
	public static PrivateKey fromPem(String data) {
		PEMReader reader = null;
		try {
			reader = new PEMReader(new StringReader(data), null, BouncyCastleLoader.getName());
			while (true) {
				Object o = reader.readObject();
				if (o instanceof PrivateKey) {
					return (PrivateKey) o;
				} else if (o instanceof KeyPair) {
					return ((KeyPair) o).getPrivate();
				} else {
					throw new IllegalArgumentException("Unexpected value found when looking for private key; found: "
							+ o.getClass());
				}
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

	public static String toPem(PrivateKey key) {
		PEMWriter pemWriter = null;
		try {
			StringWriter stringWriter = new StringWriter();
			pemWriter = new PEMWriter(stringWriter);

			pemWriter.writeObject(key);
			pemWriter.flush();

			return stringWriter.toString();
		} catch (IOException e) {
			throw new IllegalArgumentException("Error serializing key data", e);
		} finally {
			Closeables.closeQuietly(pemWriter);
		}
	}
}
