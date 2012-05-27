package org.platformlayer.ops.helpers;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.security.PublicKey;

import org.apache.log4j.Logger;
import org.openstack.utils.Io;
import org.platformlayer.ApplicationMode;
import org.platformlayer.IoUtils;
import org.platformlayer.KeyPairUtils;
import org.platformlayer.crypto.OpenSshUtils;
import org.platformlayer.crypto.RsaUtils;
import org.platformlayer.ids.ProjectId;
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

		if (ApplicationMode.isDevelopment()) {
			ProjectId project = opsContext.getUserInfo().getProjectId();

			File credentials = Io.resolve("~/.credentials");
			File ssh = new File(credentials, "ssh");
			File projectDir = new File(ssh, project.getKey());
			File keyFile = new File(projectDir, sshKeyName);

			if (!keyFile.exists()) {
				projectDir.mkdirs();

				log.warn("Writing SSH key to " + keyFile);

				try {
					String serialized = KeyPairUtils.serialize(keyPair);
					IoUtils.writeAll(keyFile, serialized);
				} catch (IOException e) {
					throw new OpsException("Error serializing SSH key", e);
				}
			}
		}

		return new SshKey(sshKeyName, user, keyPair);
	}

	public static String serialize(PublicKey sshPublicKey) throws OpsException {
		try {
			return OpenSshUtils.serialize(sshPublicKey);
		} catch (IOException e) {
			throw new OpsException("Error serializing ssh public key", e);
		}
	}
}
