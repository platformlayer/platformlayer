package org.platformlayer.ops.helpers;

import java.security.KeyPair;
import java.security.PublicKey;

import org.apache.log4j.Logger;
import org.platformlayer.crypto.OpenSshUtils;
import org.platformlayer.crypto.RsaUtils;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.ops.CloudContext;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.ServiceConfiguration;

import com.google.inject.Inject;

public class SshKeys {
	static final Logger log = Logger.getLogger(SshKeys.class);

	@Inject
	OpsContext opsContext;

	@Inject
	CloudContext cloud;

	@Deprecated
	public SshKey findOtherServiceKey(ServiceType serviceType) throws OpsException {
		ServiceConfiguration serviceConfiguration = opsContext.getServiceConfiguration();
		KeyPair sshKeyPair = serviceConfiguration.findSshKey(serviceType);
		return new SshKey(null, "root", sshKeyPair);
	}

	public SshKey getOrCreate(String sshKeyName, String user) throws OpsException {
		ServiceConfiguration serviceConfiguration = opsContext.getServiceConfiguration();
		KeyPair keyPair = serviceConfiguration.findSshKey();
		if (keyPair == null) {
			keyPair = RsaUtils.generateRsaKeyPair();
			// sshKeyPair = cloud.generateSshKeyPair(sshKeyName);
			serviceConfiguration.storeSshKeyPair(keyPair);
		}

		return new SshKey(sshKeyName, user, keyPair);
	}

	public static String serialize(PublicKey sshPublicKey) throws OpsException {
		return OpenSshUtils.serialize(sshPublicKey);
	}
}
