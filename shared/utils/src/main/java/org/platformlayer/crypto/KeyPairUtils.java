package org.platformlayer.crypto;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;
import org.platformlayer.IoUtils;
import org.platformlayer.ops.OpsException;

public class KeyPairUtils {

	public static KeyPair deserialize(String keyData) throws IOException {
		PEMReader r = new PEMReader(new StringReader(keyData), null, BouncyCastleLoader.getName());
		try {
			return (KeyPair) r.readObject();
		} finally {
			IoUtils.safeClose(r);
		}
	}

	public static String serialize(KeyPair keyPair) throws IOException {
		StringWriter writer = new StringWriter();
		PEMWriter pemWriter = new PEMWriter(writer, BouncyCastleLoader.getName());
		try {
			pemWriter.writeObject(keyPair);
			pemWriter.flush();
			return writer.toString();
		} finally {
			IoUtils.safeClose(pemWriter);
		}
	}

	public static KeyPair generateKeyPair(String algorithm, Integer keySize) throws OpsException {
		KeyPairGenerator generator;
		try {
			generator = KeyPairGenerator.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new OpsException("Error building keypair generator: " + algorithm, e);
		}
		if (keySize != null) {
			generator.initialize(keySize);
		}
		return generator.generateKeyPair();
	}

}
