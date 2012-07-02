package org.platformlayer.auth;

import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.platformlayer.auth.crypto.SecretStore;
import org.platformlayer.crypto.AesUtils;
import org.platformlayer.crypto.PasswordHash;
import org.platformlayer.crypto.RsaUtils;

@Entity
@Table(name = "users")
public class UserEntity implements OpsUser {
	public static final int TOKEN_ID_DEFAULT = 1;

	@Id
	public int id;

	@Column
	public byte[] password;

	@Column
	public byte[] secret;

	@Column
	public String key;

	@Column(name = "private_key")
	public byte[] privateKeyData;

	@Column(name = "public_key")
	public byte[] publicKeyData;

	public boolean isLocked() {
		return userSecret == null;
	}

	@Transient
	PrivateKey privateKey;

	@Override
	public PrivateKey getPrivateKey() {
		if (privateKey == null) {
			if (privateKeyData == null) {
				throw new IllegalStateException();
			}
			byte[] plaintext = AesUtils.decrypt(getUserSecret(), privateKeyData);
			privateKey = RsaUtils.deserializePrivateKey(plaintext);
		}
		return privateKey;
	}

	@Transient
	PublicKey publicKey;

	public PublicKey getPublicKey() {
		if (publicKey == null) {
			publicKey = RsaUtils.deserializePublicKey(publicKeyData);
		}
		return publicKey;
	}

	@Transient
	private SecretKey userSecret;

	@Override
	public SecretKey getUserSecret() {
		if (userSecret == null) {
			throw new IllegalStateException();
		}
		return userSecret;
	}

	public SecretKey unlockWithPassword(String password) {
		SecretStore secretStore = new SecretStore(secret);
		this.userSecret = secretStore.getSecretFromPassword(id, password);
		if (this.userSecret == null) {
			throw new SecurityException();
		}
		return this.userSecret;
	}

	@Transient
	private byte[] tokenSecret;

	public byte[] getTokenSecret() {
		if (tokenSecret == null) {
			if (userSecret == null) {
				throw new IllegalStateException();
			}

			SecretStore secretStore = new SecretStore(secret);
			this.tokenSecret = secretStore.getTokenSecretWithUserSecret(TOKEN_ID_DEFAULT, userSecret);
			if (this.tokenSecret == null) {
				throw new SecurityException();
			}
		}
		return this.tokenSecret;
	}

	public boolean isPasswordMatch(String checkPassword) {
		byte[] hashed = this.password;
		return PasswordHash.checkPasswordHash(hashed, checkPassword);
	}

	public SecretKey unlockWithToken(int tokenId, final byte[] tokenSecret) {
		SecretStore secretStore = new SecretStore(secret);
		this.userSecret = secretStore.getSecretFromToken(tokenId, tokenSecret);
		if (this.userSecret == null) {
			throw new SecurityException();
		}
		return this.userSecret;
	}

	public void unlock(SecretKey userSecret) {
		this.userSecret = userSecret;
	}

	@Override
	public int getId() {
		return id;
	}

}
