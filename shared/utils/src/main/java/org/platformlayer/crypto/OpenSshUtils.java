package org.platformlayer.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

import org.openstack.utils.Io;
import org.platformlayer.IoUtils;

import com.fathomdb.Utf8;
import com.fathomdb.hash.Md5Hash;
import com.google.common.base.Strings;

public class OpenSshUtils {
	private static final String SSH_RSA_PREFIX = "ssh-rsa ";
	private static final String KEYTYPE_RSA = "ssh-rsa";

	// TODO: I can't believe this isn't in Bouncycastle or something!!

	static class OpenSshInputStream implements Closeable {
		final InputStream is;

		static final int MAX_BUFFER_SIZE = 32768;

		public OpenSshInputStream(InputStream is) {
			super();
			this.is = is;
		}

		@Override
		public void close() throws IOException {
			is.close();
		}

		int readUint8() throws IOException {
			int v = is.read();
			if (v == -1) {
				throw new IOException("EOF");
			}
			return v;
		}

		long readUint32() throws IOException {
			long value = readUint8();
			value <<= 8;
			value |= readUint8();
			value <<= 8;
			value |= readUint8();
			value <<= 8;
			value |= readUint8();
			return value;
		}

		public byte[] readByteArray() throws IOException {
			long length = readUint32();
			if (length > MAX_BUFFER_SIZE) {
				throw new IllegalStateException();
			}
			byte[] buffer = new byte[(int) length];
			IoUtils.readFully(is, buffer, 0, (int) length);
			return buffer;
		}

		public String readString() throws IOException {
			return Utf8.toString(readByteArray());
		}

		public BigInteger readBigInteger() throws IOException {
			byte[] data = readByteArray();
			if (data.length == 0) {
				return BigInteger.ZERO;
			}

			return new BigInteger(data);
		}
	}

	static class KeyOutputStream implements Closeable {
		final OutputStream os;

		static final int MAX_BUFFER_SIZE = 32768;

		public KeyOutputStream(OutputStream os) {
			super();
			this.os = os;
		}

		@Override
		public void close() throws IOException {
			os.close();
		}

		void writeUint32(long value) throws IOException {
			byte[] tmp = new byte[4];
			tmp[0] = (byte) ((value >>> 24) & 0xff);
			tmp[1] = (byte) ((value >>> 16) & 0xff);
			tmp[2] = (byte) ((value >>> 8) & 0xff);
			tmp[3] = (byte) (value & 0xff);
			os.write(tmp);
		}

		public void writeByteArray(byte[] data) throws IOException {
			writeUint32(data.length);
			os.write(data);
		}

		public void writeString(String data) throws IOException {
			writeByteArray(Utf8.getBytes(data));
		}

		public void writeBigInteger(BigInteger value) throws IOException {
			if (value.equals(BigInteger.ZERO)) {
				writeUint32(0);
			} else {
				writeByteArray(value.toByteArray());
			}
		}
	}

	public static PublicKey readSshPublicKey(String sshPublicKey) throws IOException {
		if (Strings.isNullOrEmpty(sshPublicKey)) {
			return null;
			// StringReader reader = new StringReader(sshPublicKey);
			// PEMReader pemReader = new PEMReader(reader);
			// return (PublicKey) pemReader.readObject();
		}

		if (sshPublicKey.startsWith(SSH_RSA_PREFIX)) {
			String base64 = sshPublicKey.substring(SSH_RSA_PREFIX.length());
			byte[] data = CryptoUtils.fromBase64(base64);

			OpenSshInputStream is = new OpenSshInputStream(new ByteArrayInputStream(data));
			try {
				String keyType = is.readString();

				if (keyType.equals(KEYTYPE_RSA)) {
					final BigInteger publicExponent = is.readBigInteger();
					final BigInteger modulus = is.readBigInteger();

					final RSAPublicKeySpec rsaPubSpec = new RSAPublicKeySpec(modulus, publicExponent);

					try {
						KeyFactory rsaKeyFact = KeyFactory.getInstance("RSA");
						return rsaKeyFact.generatePublic(rsaPubSpec);
					} catch (NoSuchAlgorithmException e) {
						throw new IllegalStateException("Error loading RSA provider", e);
					} catch (InvalidKeySpecException e) {
						throw new IOException("Key data is corrupted", e);
					}
				} else {
					throw new IOException("Unhandled key type: " + keyType);
				}
			} finally {
				Io.safeClose(is);
			}
		} else {
			throw new IOException("Unknown key format: " + sshPublicKey);
		}
	}

	static byte[] encodePublicKey(RSAPublicKey key) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			KeyOutputStream out = new KeyOutputStream(baos);
			try {
				out.writeString(KEYTYPE_RSA);
				out.writeBigInteger(key.getPublicExponent());
				out.writeBigInteger(key.getModulus());
			} finally {
				out.close();
			}
			return baos.toByteArray();
		} catch (IOException e) {
			throw new IllegalArgumentException("Invalid public key (error while serializing)", e);
		}
	}

	public static String serialize(PublicKey sshPublicKey) {
		return serialize(sshPublicKey, null);
	}

	public static String serialize(PublicKey sshPublicKey, String description) {
		if (sshPublicKey == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(SSH_RSA_PREFIX);
		sb.append(CryptoUtils.toBase64(encodePublicKey((RSAPublicKey) sshPublicKey)));
		if (description != null) {
			sb.append(" ");
			sb.append(description);
		}
		return sb.toString();
	}

	public static String getSignatureString(PublicKey publicKey) {
		Md5Hash sig = getSignature(publicKey);

		String sigString = sig.toHex().toLowerCase();
		return sigString;
	}

	public static Md5Hash getSignature(PublicKey publicKey) {
		byte[] data = encodePublicKey((RSAPublicKey) publicKey);

		Md5Hash sig = new Md5Hash.Hasher().hash(data);
		return sig;
	}
}
