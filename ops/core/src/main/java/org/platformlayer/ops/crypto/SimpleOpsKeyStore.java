package org.platformlayer.ops.crypto;

import java.io.File;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;

import org.platformlayer.crypto.RsaUtils;

import com.google.common.collect.Maps;

public class SimpleOpsKeyStore implements OpsKeyStore {
	static final File SECRETS_ROOT = new File("/var/secrets/");

	static final Map<Integer, PrivateKey> privateKeys = Maps.newHashMap();
	static final Map<Integer, PublicKey> publicKeys = Maps.newHashMap();

	public SimpleOpsKeyStore() {
		try {
			loadAllKeys(SECRETS_ROOT);
		} catch (IOException e) {
			throw new IllegalStateException("Error loading keys", e);
		}
	}

	private void loadAllKeys(File dir) throws IOException {
		if (!dir.exists()) {
			return;
		}

		for (File file : dir.listFiles(new FilenameEndsWithFilter(".pub"))) {
			PublicKey publicKey = RsaUtils.loadPublicKey(file);
			String name = file.getName();
			name = name.replace(".pub", "");
			publicKeys.put(Integer.parseInt(name), publicKey);
		}

		for (File file : dir.listFiles(new FilenameEndsWithFilter(".private"))) {
			PrivateKey privateKey = RsaUtils.loadPrivateKey(file);
			String name = file.getName();
			name = name.replace(".private", "");
			privateKeys.put(Integer.parseInt(name), privateKey);
		}
	}

	@Override
	public PrivateKey findPrivateKey(int backendId) {
		return privateKeys.get(backendId);
	}

	@Override
	public PublicKey findPublicKey(int backendId) {
		return publicKeys.get(backendId);
	}

	@Override
	public Iterable<Integer> getBackends() {
		return publicKeys.keySet();
	}

	// @Override
	// public SecretKey findUserSecret(int userId) {
	// UserInfo userInfo = OpsContext.get().getUserInfo();
	// if (userInfo != null && userInfo.getUserId() == userId) {
	// return userInfo.findUserSecret();
	// }
	// return null;
	// }
	//
	// @Override
	// public Iterable<Integer> getProjectIds() {
	// List<Integer> projectIds = Lists.newArrayList();
	//
	// UserInfo userInfo = OpsContext.get().getUserInfo();
	// if (userInfo != null) {
	// OpsProject project = userInfo.getOpsProject();
	// projectIds.add( project.id);
	// }
	// return projectIds;
	// }

}