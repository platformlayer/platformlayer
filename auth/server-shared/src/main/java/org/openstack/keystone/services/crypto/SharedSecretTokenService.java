package org.openstack.keystone.services.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;

import org.openstack.keystone.services.TokenInfo;
import org.openstack.keystone.services.TokenService;
import org.openstack.utils.Utf8;
import org.platformlayer.IoUtils;
import org.platformlayer.crypto.CryptoUtils;
import org.platformlayer.crypto.SecureComparison;

public class SharedSecretTokenService implements TokenService {
	private final SecretKeySpec systemSecretKeySpec;
	private final SecretKeySpec userSecretKeySpec;

	// To keep the numbers smaller; we quantize time and offset it
	static final long TIME_GRANULARITY = 60000L;
	static final long TIME_OFFSET = 1234567890;

	public SharedSecretTokenService(String secret) {
		// I don't think there's actually any benefit to using different keys
		this.systemSecretKeySpec = CryptoUtils.deriveHmacSha1Key(secret);
		this.userSecretKeySpec = CryptoUtils.deriveHmacSha1Key(secret);
	}

	@Override
	public TokenInfo decodeToken(boolean system, String token) {
		if (token == null) {
			return null;
		}

		try {
			String base64 = unescapeBase64(token);
			byte[] buffer = CryptoUtils.fromBase64(base64);

			ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
			byte flags = (byte) bais.read();
			if (flags == -1) {
				return null;
			}

			String scope = readNullTerminatedString(bais);
			if (scope.length() == 0) {
				scope = null;
			}

			String expiration = readNullTerminatedString(bais);
			String username = readNullTerminatedString(bais);
			byte[] tokenSecret = readLengthPrefixByteArray(bais);

			byte[] signature = new byte[CryptoUtils.HMAC_SHA1_BYTES];
			if (bais.read(signature) != CryptoUtils.HMAC_SHA1_BYTES) {
				return null;
			}

			SecretKeySpec secretKeySpec = system ? systemSecretKeySpec : userSecretKeySpec;
			byte[] actualSignature = CryptoUtils.hmacSha1(secretKeySpec, buffer, 0, buffer.length
					- CryptoUtils.HMAC_SHA1_BYTES);

			if (!SecureComparison.equal(actualSignature, signature)) {
				return null;
			}

			long roundedTime = Long.parseLong(expiration, 16);
			long time = (roundedTime * TIME_GRANULARITY) + TIME_OFFSET;

			return new TokenInfo(flags, scope, username, new Date(time), tokenSecret);
		} catch (Exception e) {
			return null;
		}
	}

	private static String readNullTerminatedString(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while (true) {
			int nextByte = is.read();
			if (nextByte == -1) {
				break;
			}
			if (nextByte == 0) {
				break;
			}
			baos.write(nextByte);
		}

		return Utf8.toString(baos);
	}

	private static byte[] readLengthPrefixByteArray(InputStream is) throws IOException {
		int length = is.read();
		if (length == -1) {
			throw new EOFException();
		}

		if (length == 0) {
			return null;
		}

		length--;

		byte[] data = new byte[length];
		IoUtils.readFully(is, data, 0, length);
		return data;
	}

	@Override
	public String encodeToken(TokenInfo tokenInfo) {
		long time = tokenInfo.expiration.getTime();

		long roundedTime = ((time - TIME_OFFSET) + TIME_GRANULARITY - 1) / TIME_GRANULARITY;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			baos.write(tokenInfo.flags);
			if (tokenInfo.scope != null) {
				baos.write(Utf8.getBytes(tokenInfo.scope));
			}
			baos.write(0);
			baos.write(Utf8.getBytes(Long.toHexString(roundedTime)));
			baos.write(0);
			baos.write(Utf8.getBytes(tokenInfo.userId));
			baos.write(0);
			if (tokenInfo.tokenSecret != null) {
				if (tokenInfo.tokenSecret.length >= 100) {
					// We might want to use a variable length integer encoding in future
					throw new IllegalStateException();
				}
				baos.write(tokenInfo.tokenSecret.length + 1);
				baos.write(tokenInfo.tokenSecret);
			} else {
				baos.write(0);
			}
		} catch (IOException e) {
			throw new IllegalStateException();
		}

		SecretKeySpec secretKeySpec = tokenInfo.isSystem() ? systemSecretKeySpec : userSecretKeySpec;
		byte[] signed = CryptoUtils.hmacSha1(secretKeySpec, baos.toByteArray());
		if (signed.length != CryptoUtils.HMAC_SHA1_BYTES) {
			throw new IllegalStateException();
		}

		try {
			baos.write(signed);
		} catch (IOException e) {
			throw new IllegalStateException();
		}

		String base64 = CryptoUtils.toBase64(baos.toByteArray());
		String encoded = escapeBase64(base64);

		return encoded;
	}

	static String escapeBase64(String s) {
		s = s.replace('+', '_');
		s = s.replace('/', '.');
		s = s.replace('=', '-');
		return s;
	}

	static String unescapeBase64(String s) {
		s = s.replace('_', '+');
		s = s.replace('.', '/');
		s = s.replace('-', '=');
		return s;
	}

}
