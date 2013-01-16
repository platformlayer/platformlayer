package org.platformlayer.ops.helpers;

import java.security.KeyPair;
import java.security.PublicKey;

import org.slf4j.*;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.ops.CloudContext;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.ServiceConfiguration;

import com.fathomdb.crypto.OpenSshUtils;
import com.google.inject.Inject;

public class SshKeys {
	static final Logger log = LoggerFactory.getLogger(SshKeys.class);

	@Inject
	OpsContext opsContext;

	@Inject
	CloudContext cloud;

	@Inject
	PrivateDataHelper privateData;

	@Deprecated
	public SshKey findOtherServiceKey(ServiceType serviceType) throws OpsException {
		ServiceConfiguration serviceConfiguration = opsContext.getServiceConfiguration();
		KeyPair sshKeyPair = privateData.findSshKey(serviceConfiguration.getProject(), serviceType);
		return new SshKey(null, "root", sshKeyPair);
	}

	public SshKey getOrCreate(String sshKeyName, String user) throws OpsException {
		ServiceConfiguration serviceConfiguration = opsContext.getServiceConfiguration();
		return privateData.getOrCreate(serviceConfiguration.getProject(), serviceConfiguration.getServiceType(),
				sshKeyName, user);
	}

	public static String serialize(PublicKey sshPublicKey) throws OpsException {
		return OpenSshUtils.serialize(sshPublicKey);
	}
}
