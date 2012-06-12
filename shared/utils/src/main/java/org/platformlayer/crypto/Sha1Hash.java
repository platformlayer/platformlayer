package org.platformlayer.crypto;

import org.openstack.crypto.StronglyTypedHash;
import org.openstack.utils.Hex;

public class Sha1Hash extends StronglyTypedHash {
	private static final int SHA1_BYTE_LENGTH = 160 / 8;

	public Sha1Hash(String md5String) {
		this(Hex.fromHex(md5String));
	}

	public Sha1Hash(byte[] sha) {
		super(sha);

		if (sha.length != SHA1_BYTE_LENGTH) {
			throw new IllegalArgumentException();
		}
	}
}
