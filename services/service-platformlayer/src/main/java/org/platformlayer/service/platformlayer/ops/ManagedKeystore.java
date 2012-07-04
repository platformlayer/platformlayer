package org.platformlayer.service.platformlayer.ops;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.openstack.crypto.KeyStoreUtils;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.TagChanges;
import org.platformlayer.crypto.CryptoUtils;
import org.platformlayer.ops.FileUpload;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.tree.OpsTreeBase;

import sun.security.x509.X500Name;

import com.google.common.collect.Lists;

public class ManagedKeystore extends OpsTreeBase {
	public File path;

	public String keystoreSecret = "notasecret";

	public ItemBase tagWithPublicKeys;

	private static final Logger log = Logger.getLogger(ManagedKeystore.class);

	@Inject
	PlatformLayerHelpers platformlayer;

	@Handler
	public void handler(OpsTarget target) throws OpsException {
		KeyStore keystore = null;
		boolean dirty = false;

		List<String> keyAliases;

		{
			byte[] data = target.readBinaryFile(path);
			try {
				if (data != null) {
					keystore = KeyStoreUtils.load(data, keystoreSecret);
				} else {
					keystore = KeyStoreUtils.createEmpty(keystoreSecret);
					dirty = true;
				}

				keyAliases = KeyStoreUtils.getKeyAliases(keystore);

			} catch (GeneralSecurityException e) {
				throw new OpsException("Error reading keystore", e);
			} catch (IOException e) {
				throw new OpsException("Error reading keystore", e);
			}
		}

		if (keyAliases.size() == 0) {
			String alias = "selfsigned";
			String keyPassword = "notasecret";

			int validityDays = 365 * 10;
			String subjectDN = "CN=platformlayer";

			X500Name x500Name;
			try {
				x500Name = new X500Name(subjectDN);
			} catch (IOException e) {
				throw new OpsException("Error building X500 name", e);
			}

			try {
				KeyStoreUtils.createSelfSigned(keystore, alias, keyPassword, x500Name, validityDays);
			} catch (GeneralSecurityException e) {
				throw new OpsException("Error creating self-signed certificate", e);
			}
			dirty = true;
			keyAliases.add(alias);
		}

		if (tagWithPublicKeys != null) {
			List<String> publicKeySigs = Lists.newArrayList();

			try {
				for (String alias : keyAliases) {
					Certificate[] cert = keystore.getCertificateChain(alias);
					if (cert.length == 0) {
						log.warn("Ignoring zero length certificate chain for: " + alias);
						continue;
					}
					PublicKey certPublicKey = cert[0].getPublicKey();

					String sigString = CryptoUtils.getSignature(certPublicKey);
					publicKeySigs.add(sigString);
				}
			} catch (GeneralSecurityException e) {
				throw new OpsException("Error reading public keys", e);
			}

			List<String> existingSigs = Tag.PUBLIC_KEY_SIG.find(tagWithPublicKeys);

			List<String> missing = Lists.newArrayList();
			for (String publicKeySig : publicKeySigs) {
				if (!existingSigs.contains(publicKeySig)) {
					missing.add(publicKeySig);
				}
			}

			if (!missing.isEmpty()) {
				TagChanges tagChanges = new TagChanges();
				for (String add : missing) {
					tagChanges.addTags.add(Tag.PUBLIC_KEY_SIG.build(add));
				}
				platformlayer.changeTags(tagWithPublicKeys.getKey(), tagChanges);
			}
		}

		if (dirty) {
			byte[] data;
			try {
				data = KeyStoreUtils.serialize(keystore, keystoreSecret);
			} catch (GeneralSecurityException e) {
				throw new OpsException("Error serializing keystore", e);
			} catch (IOException e) {
				throw new OpsException("Error serializing keystore", e);
			}
			FileUpload.upload(target, path, data);
		}
	}

	@Override
	protected void addChildren() throws OpsException {

	}
}
