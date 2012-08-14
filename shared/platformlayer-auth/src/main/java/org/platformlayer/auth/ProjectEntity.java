package org.platformlayer.auth;

import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.openstack.utils.Utf8;
import org.platformlayer.auth.crypto.SecretStore;
import org.platformlayer.crypto.AesUtils;
import org.platformlayer.crypto.RsaUtils;
import org.platformlayer.crypto.SecureComparison;

@Entity
@Table(name = "projects")
public class ProjectEntity implements ProjectInfo {
	static final Logger log = Logger.getLogger(ProjectEntity.class);

	@Id
	public int id;

	@Column(name = "key")
	public String name;

	@Column(name = "secret")
	public byte[] secretData;

	// Currently this column just serves as a check that the secret is valid
	@Column(name = "metadata")
	public byte[] metadata;

	@Transient
	public SecretKey projectSecret;

	@Column(name = "private_key")
	public byte[] privateKeyData;

	@Column(name = "public_key")
	public byte[] publicKeyData;

	@Override
	public boolean isLocked() {
		return projectSecret == null;
	}

	@Override
	public SecretKey getProjectSecret() {
		if (projectSecret == null) {
			throw new IllegalStateException();
		}
		return projectSecret;
	}

	public void unlockWithUser(UserEntity user) {
		SecretStore secretStore = new SecretStore(this.secretData);
		this.projectSecret = secretStore.getSecretFromUser(user);
		if (this.projectSecret == null) {
			throw new SecurityException();
		}
	}

	public void unlockWithProject(ProjectEntity project) {
		SecretStore secretStore = new SecretStore(this.secretData);
		this.projectSecret = secretStore.getSecretFromProject(project);
		if (this.projectSecret == null) {
			throw new SecurityException();
		}
	}

	public void setProjectSecret(SecretKey secret) {
		this.projectSecret = secret;
	}

	public boolean isSecretValid() {
		try {
			byte[] plaintext = AesUtils.decrypt(getProjectSecret(), metadata);
			String prefix = name + "\0";
			byte[] prefixBytes = Utf8.getBytes(prefix);
			if (!SecureComparison.startsWith(plaintext, prefixBytes)) {
				return false;
			}
			return true;
		} catch (Exception e) {
			log.debug("Unexpected error while checking secret", e);
			return false;
		}
	}

	@Transient
	PrivateKey privateKey;

	public PrivateKey getPrivateKey() {
		if (privateKey == null) {
			if (privateKeyData == null) {
				throw new IllegalStateException();
			}
			byte[] plaintext = AesUtils.decrypt(getProjectSecret(), privateKeyData);
			privateKey = RsaUtils.deserializePrivateKey(plaintext);
		}
		return privateKey;
	}

	public void setPrivateKey(PrivateKey privateKey) {
		byte[] privateKeyData = RsaUtils.serialize(privateKey);
		privateKeyData = AesUtils.encrypt(getProjectSecret(), privateKeyData);
		this.privateKeyData = privateKeyData;
		this.privateKey = privateKey;
	}

	@Transient
	PublicKey publicKey;

	public PublicKey getPublicKey() {
		if (publicKey == null) {
			publicKey = RsaUtils.deserializePublicKey(publicKeyData);
		}
		return publicKey;
	}

	public void setPublicKey(PublicKey publicKey) {
		this.publicKeyData = RsaUtils.serialize(publicKey);
		this.publicKey = publicKey;
	}

	@Override
	public String toString() {
		return "ProjectEntity [key=" + name + "]";
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getId() {
		return id;
	}

}
