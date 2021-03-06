package org.platformlayer.auth.services.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.crypto.interfaces.PBEKey;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;

import org.platformlayer.auth.services.TokenInfo;
import org.platformlayer.auth.services.TokenService;
import org.platformlayer.crypto.CryptoUtils;
import org.platformlayer.crypto.SecureComparison;
import org.platformlayer.metrics.Instrumented;

import com.fathomdb.Configuration;
import com.fathomdb.Utf8;
import com.fathomdb.crypto.KeyDerivationFunctions;
import com.fathomdb.utils.Base64;
import com.google.common.io.ByteStreams;

@Instrumented
public class SharedSecretTokenService implements TokenService {
	private final SecretKeySpec userSecretKeySpec;

	// To keep the numbers smaller; we quantize time and offset it
	static final long TIME_GRANULARITY = 60000L;
	static final long TIME_OFFSET = 1234567890;

	public static class Provider implements javax.inject.Provider<SharedSecretTokenService> {

		@Inject
		Configuration configuration;

		@Override
		public SharedSecretTokenService get() {
			String secret = configuration.find("sharedsecret");
			if (secret == null) {
				throw new IllegalStateException("sharedsecret is required");
			}
			SharedSecretTokenService tokenService = new SharedSecretTokenService(secret);
			return tokenService;
		}
	}

	static SecretKeySpec deriveHmacSha1Key(String keyData) {
		// We want a consistent salt; it can't be empty
		byte[] salt = Utf8.getBytes(keyData);
		int keySize = 128; // ??
		int iterationCount = 1000;
		PBEKey pbeKey = KeyDerivationFunctions.doPbkdf2(iterationCount, salt, keyData, keySize);

		return CryptoUtils.buildHmacSha1Key(pbeKey.getEncoded());
	}

	public SharedSecretTokenService(String secret) {
		this.userSecretKeySpec = deriveHmacSha1Key(secret);
	}

	@Override
	public TokenInfo decodeToken(String token) {
		if (token == null) {
			return null;
		}

		try {
			String base64 = unescapeBase64(token);
			byte[] buffer = Base64.decode(base64);

			ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
			byte flags = (byte) bais.read();
			if (flags == -1) {
				return null;
			}

			String expiration = readNullTerminatedString(bais);
			String username = readNullTerminatedString(bais);
			byte[] tokenSecret = readLengthPrefixByteArray(bais);

			byte[] signature = new byte[CryptoUtils.HMAC_SHA1_BYTES];
			if (bais.read(signature) != CryptoUtils.HMAC_SHA1_BYTES) {
				return null;
			}

			SecretKeySpec secretKeySpec = userSecretKeySpec;
			byte[] actualSignature = CryptoUtils.hmacSha1(secretKeySpec, buffer, 0, buffer.length
					- CryptoUtils.HMAC_SHA1_BYTES);

			if (!SecureComparison.equal(actualSignature, signature)) {
				return null;
			}

			long roundedTime = Long.parseLong(expiration, 16);
			long time = (roundedTime * TIME_GRANULARITY) + TIME_OFFSET;

			return new TokenInfo(flags, username, new Date(time), tokenSecret);
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
		ByteStreams.readFully(is, data, 0, length);
		return data;
	}

	@Override
	public String encodeToken(TokenInfo tokenInfo) {
		long time = tokenInfo.expiration.getTime();

		long roundedTime = ((time - TIME_OFFSET) + TIME_GRANULARITY - 1) / TIME_GRANULARITY;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			baos.write(tokenInfo.flags);
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

		SecretKeySpec secretKeySpec = userSecretKeySpec;
		byte[] signed = CryptoUtils.hmacSha1(secretKeySpec, baos.toByteArray());
		if (signed.length != CryptoUtils.HMAC_SHA1_BYTES) {
			throw new IllegalStateException();
		}

		try {
			baos.write(signed);
		} catch (IOException e) {
			throw new IllegalStateException();
		}

		String base64 = Base64.encode(baos.toByteArray());
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
