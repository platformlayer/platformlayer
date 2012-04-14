package org.openstack.service.nexus.utils;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.bouncycastle.util.encoders.Base64Encoder;
import org.junit.Test;
import org.platformlayer.service.nexus.utils.NexusLdapPasswords;

public class NexusLdapPasswordsTest {

	@Test
	public void testDecryption() throws Exception {
		NexusLdapPasswords passwords = new NexusLdapPasswords();

		String plaintext = "adminsecret";
		String ciphertext = "CIj2qAUHmLHvrlRXsW9Z2dfsGm0=";

		assertStructure(ciphertext);

		String decrypted = passwords.decrypt(ciphertext);

		assertEquals(plaintext, decrypted);
	}

	private void assertStructure(String ciphertext) throws IOException {
		if (ciphertext.startsWith("{") && ciphertext.endsWith("}")) {
			ciphertext = ciphertext.substring(1, ciphertext.length() - 1);
		}

		Base64Encoder decoder = new Base64Encoder();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		decoder.decode(ciphertext, baos);
		byte[] res = baos.toByteArray();
		assertEquals(8, res[0]);
	}

	@Test
	public void testEncryption() throws Exception {
		NexusLdapPasswords passwords = new NexusLdapPasswords();

		String plaintext = "adminsecret";
		String ciphertext = passwords.encrypt(plaintext);
		System.out.println("ciphertext = " + ciphertext);

		assertStructure(ciphertext);

		String decrypted = passwords.decrypt(ciphertext);

		assertEquals(plaintext, decrypted);
	}

}
