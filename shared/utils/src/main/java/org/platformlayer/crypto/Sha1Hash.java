package org.platformlayer.crypto;

public class Sha1Hash extends StronglyTypedHash {
	private static final int SHA1_BYTE_LENGTH = 160 / 8;

	public Sha1Hash(String md5String) {
		this(CryptoUtils.fromHex(md5String));
	}

	public Sha1Hash(byte[] md5) {
		super(md5);

		if (md5.length != SHA1_BYTE_LENGTH) {
			throw new IllegalArgumentException();
		}
	}
}
