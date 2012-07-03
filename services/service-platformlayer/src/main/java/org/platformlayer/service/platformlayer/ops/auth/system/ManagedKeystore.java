package org.platformlayer.service.platformlayer.ops.auth.system;

import java.io.File;
import java.security.KeyStore;
import java.util.List;

import org.apache.log4j.Logger;
import org.openstack.crypto.KeyStoreUtils;
import org.platformlayer.ops.FileUpload;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.tree.OpsTreeBase;

import sun.security.x509.X500Name;

public class ManagedKeystore extends OpsTreeBase {
	File path;

	String keystoreSecret = "notasecret";

	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(ManagedKeystore.class);

	@Handler
	public void handler(OpsTarget target) throws Exception {
		KeyStore keystore = null;
		boolean dirty = false;

		{
			byte[] data = target.readBinaryFile(path);
			if (data != null) {
				keystore = KeyStoreUtils.load(data, keystoreSecret);
			} else {
				keystore = KeyStoreUtils.createEmpty(keystoreSecret);
				dirty = true;
			}
		}

		List<String> keyAliases = KeyStoreUtils.getKeyAliases(keystore);
		if (keyAliases.size() == 0) {
			String alias = "selfsigned";
			String keyPassword = "notasecret";

			int validityDays = 365 * 10;
			String subjectDN = "CN=platformlayer";

			X500Name x500Name = new X500Name(subjectDN);
			KeyStoreUtils.createSelfSigned(keystore, alias, keyPassword, x500Name, validityDays);
			dirty = true;
		}

		if (dirty) {
			byte[] data = KeyStoreUtils.serialize(keystore, keystoreSecret);
			FileUpload.upload(target, path, data);
		}

	}

	@Override
	protected void addChildren() throws OpsException {

	}
}
