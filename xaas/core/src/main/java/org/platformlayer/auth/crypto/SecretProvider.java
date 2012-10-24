package org.platformlayer.auth.crypto;

import org.platformlayer.auth.ProjectInfo;
import org.platformlayer.core.model.SecretInfo;
import org.platformlayer.model.ProjectAuthorization;

import com.fathomdb.crypto.CryptoKey;

public abstract class SecretProvider {

	public abstract CryptoKey getItemSecret(SecretInfo secret);

	public static SecretProvider from(final ProjectInfo project) {
		if (project == null) {
			throw new IllegalArgumentException();
		}

		return new SecretProvider() {
			@Override
			public CryptoKey getItemSecret(SecretInfo secret) {
				SecretStore secretStore = new SecretStore(secret.getEncoded());

				CryptoKey secretKey = secretStore.getSecretFromProject(project);
				return secretKey;
			}
		};
	}

	public static SecretProvider from(final ProjectAuthorization project) {
		if (project == null) {
			throw new IllegalArgumentException();
		}

		return new SecretProvider() {
			@Override
			public CryptoKey getItemSecret(SecretInfo secret) {
				SecretStore secretStore = new SecretStore(secret.getEncoded());

				CryptoKey secretKey = secretStore.getSecretFromProject(project);
				return secretKey;
			}
		};
	}

	// public static SecretProvider withAuth(OpsAuthentication auth) {
	// return withProject(auth.getProject());
	// }

	public static SecretProvider forKey(final CryptoKey itemSecret) {
		return new SecretProvider() {
			@Override
			public CryptoKey getItemSecret(SecretInfo secret) {
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
