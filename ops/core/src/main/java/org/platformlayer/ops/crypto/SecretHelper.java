package org.platformlayer.ops.crypto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.PublicKey;

import javax.crypto.SecretKey;
import javax.inject.Inject;

import org.platformlayer.auth.OpsProject;
import org.platformlayer.auth.crypto.SecretStore;
import org.platformlayer.crypto.AesUtils;
import org.platformlayer.ops.OpsContext;

public class SecretHelper {
	@Inject
	OpsKeyStore keyStore;

	// TODO: We need to use the project secret, not the item secret

	public byte[] encodeItemSecret(SecretKey itemSecret) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			SecretStore.Writer writer = new SecretStore.Writer(baos);

			byte[] plaintext = AesUtils.serialize(itemSecret);

			for (int backend : keyStore.getBackends()) {
				PublicKey publicKey = keyStore.findPublicKey(backend);
				if (publicKey != null) {
					writer.writeAsymetricKey(plaintext, backend, publicKey);
				} else {
					throw new IllegalStateException();
				}
			}

			for (OpsProject project : OpsContext.get().getEncryptingProjects()) {
				if (project.isLocked()) {
					throw new IllegalStateException();
					// {
					// UserInfo user = OpsContext.get().getUserInfo();
					// ProjectId projectId = user.getProjectId();
					// OpsProject project = userRepository.findProjectByKey(projectId.getKey());
					// if (project == null) {
					// throw new IllegalStateException("Project not found");
					// }
					//
					// OpsUser opsUser = userRepository.findUser(user.getUserKey());
					// if (project == null) {
					// throw new IllegalStateException("User not found");
					// }
					//
					// SecretStore secretStore = new SecretStore(project.secretData);
					// projectKey = secretStore.getSecretFromUser(opsUser);
					//
					// project.unlockWithUser(opsUser);
					//
					// SecretKey projectSecret = project.getProjectSecret();
					// }
				}

				writer.writeLockedByProjectKey(plaintext, project.getId(), project.getProjectSecret());
			}

			// for (int userId : keyStore.getProjectIds()) {
			// SecretKey secretKey = keyStore.findUserSecret(userId);
			// if (secretKey != null) {
			// writer.writeLockedByUserKey(plaintext, userId, secretKey);
			// } else {
			// throw new IllegalStateException();
			// }
			// }

			writer.close();
			baos.close();

			return baos.toByteArray();
		} catch (IOException e) {
			throw new IllegalStateException("Error serializing key", e);
		}
	}

	// public SecretKey decodeSecret(byte[] encoded) {
	// SecretStoreDecoder visitor = new SecretStoreDecoder() {
	// @Override
	// public void visitAsymetricSystemKey(int keyId, byte[] data) {
	// PrivateKey privateKey = keyStore.findPrivateKey(keyId);
	// if (privateKey != null) {
	// setSecretKey(decryptAsymetricKey(privateKey, data));
	// }
	// }
	//
	// @Override
	// public void visitUserKey(int userId, byte[] data) {
	// SecretKey userKey = keyStore.findUserSecret(userId);
	// if (userKey != null) {
	// setSecretKey(decryptSymetricKey(userKey, data));
	// }
	// }
	// };
	//
	// try {
	// SecretStore.read(encoded, visitor);
	// } catch (IOException e) {
	// throw new IllegalArgumentException("Error deserializing secret", e);
	// }
	//
	// SecretKey secretKey = visitor.getSecretKey();
	// if (secretKey == null)
	// throw new IllegalArgumentException("Cannot decrypt secret");
	// return secretKey;
	//
	// }

	public byte[] decryptSecret(byte[] data, byte[] secret) {
		SecretStore secretStore = new SecretStore(secret);

		SecretKey secretKey = null;

		for (OpsProject project : OpsContext.get().getEncryptingProjects()) {
			secretKey = secretStore.getSecretFromProject(project);
		}

		if (secretKey == null) {
			throw new SecurityException();
		}

		return AesUtils.decrypt(secretKey, data);
	}

}
