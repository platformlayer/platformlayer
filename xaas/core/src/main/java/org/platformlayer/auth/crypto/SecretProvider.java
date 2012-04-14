package org.platformlayer.auth.crypto;

import javax.crypto.SecretKey;

import org.platformlayer.auth.OpsProject;
import org.platformlayer.core.model.SecretInfo;
import org.platformlayer.ops.auth.OpsAuthentication;

public abstract class SecretProvider {

	public abstract SecretKey getItemSecret(SecretInfo secret);

	public static SecretProvider withProject(final OpsProject project) {
		if (project == null) {
			throw new IllegalArgumentException();
		}

		return new SecretProvider() {
			@Override
			public SecretKey getItemSecret(SecretInfo secret) {
				SecretStore secretStore = new SecretStore(secret.getEncoded());

				SecretKey secretKey = secretStore.getSecretFromProject(project.id, project.getProjectSecret());
				return secretKey;
			}
		};
	}

	public static SecretProvider withAuth(OpsAuthentication auth) {
		return withProject(auth.getProject());
	}

	public static SecretProvider forKey(final SecretKey itemSecret) {
		return new SecretProvider() {
			@Override
			public SecretKey getItemSecret(SecretInfo secret) {
				return itemSecret;
			}
		};
	}

	// public static void unlockItemWithCurrentUser(SecretInfo secretInfo) {
	// UserInfo userInfo = OpsContext.get().getUserInfo();
	// if (userInfo == null) {
	// throw new SecurityException();
	// }
	//
	// unlockItem(secretInfo, userInfo.getProject());
	// }

	// public static void unlockItem(SecretInfo secret, OpsAuthentication auth) {
	// unlockItem(secret, auth.getProject());
	// }

}
