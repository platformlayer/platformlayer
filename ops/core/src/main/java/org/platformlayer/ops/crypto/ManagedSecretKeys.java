package org.platformlayer.ops.crypto;

import java.security.KeyPair;
import java.util.List;

import javax.inject.Inject;
import javax.security.auth.x500.X500Principal;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.crypto.RsaUtils;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.ProviderHelper;
import org.platformlayer.ops.helpers.ProviderHelper.ProviderOf;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class ManagedSecretKeys {
	public static final String TAG_KEY_ID = "key.alias";

	private static final Logger log = LoggerFactory.getLogger(ManagedSecretKeys.class);

	@Inject
	protected PlatformLayerHelpers platformLayer;

	@Inject
	protected ProviderHelper providers;

	public ManagedSecretKey findSslKey(PlatformLayerKey owner, PlatformLayerKey sslKey, String keyId)
			throws OpsException {
		{
			// Check for existing key
			List<ProviderOf<ManagedSecretKey>> keyProviders = providers.listChildrenProviding(owner,
					ManagedSecretKey.class);

			List<ManagedSecretKey> matchingTag = Lists.newArrayList();

			for (ProviderOf<ManagedSecretKey> keyProvider : keyProviders) {
				ItemBase item = keyProvider.getItem();
				if (!item.getTags().hasTag(TAG_KEY_ID, keyId)) {
					continue;
				}
				matchingTag.add(keyProvider.get());
			}

			if (matchingTag.size() > 1) {
				// TODO: Pick the longest time-to-expiry key
				log.warn("Found multiple keys as children of {}", owner);
			}

			// Return existing key
			for (ManagedSecretKey key : matchingTag) {
				return key;
			}
		}

		ManagedSecretKey ca;

		{
			ItemBase sslKeyItem = (ItemBase) platformLayer.getItem(sslKey);
			ManagedSecretKey key = providers.toInterface(sslKeyItem, ManagedSecretKey.class);

			if (!key.isCaKey()) {
				// Easy case - we just want to use this key
				return key;
			}
			ca = key;
		}

		if (!OpsContext.isConfigure()) {
			log.info("No SSL key found; not in configure mode; won't create");
			return null;
		}

		// No key found; let's create a new key
		log.info("No SSL key found; creating a new one under {}", owner);

		{
			X500Principal subject = buildX500(keyId, owner);
			KeyPair keyPair = RsaUtils.generateRsaKeyPair();

			PlatformLayerKey createdPath = ca.createSignedKey(owner, keyId, subject, keyPair);
			ItemBase createdModel = platformLayer.getItem(createdPath);
			ManagedSecretKey created = providers.toInterface(createdModel, ManagedSecretKey.class);
			return created;
		}

	}

	public static X500Principal buildX500(String keyId, PlatformLayerKey owner) {
		X500PrincipalBuilder builder = new X500PrincipalBuilder();
		builder.addCn(keyId);
		builder.addCn(owner.getItemIdString());
		builder.addCn(owner.getItemTypeString());
		builder.addCn(owner.getServiceTypeString());
		builder.addCn(owner.getProjectString());

		return builder.build();

	}
}
