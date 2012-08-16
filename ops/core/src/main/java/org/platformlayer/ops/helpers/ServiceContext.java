package org.platformlayer.ops.helpers;

import javax.inject.Inject;
import javax.security.auth.x500.X500Principal;

import org.platformlayer.ids.ServiceType;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;

public class ServiceContext {
	@Inject
	OpsContext opsContext;

	@Inject
	SshKeys sshKeys;

	public ServiceContext() {
	}

	public SshKey getSshKey() throws OpsException {
		String sshKeyName = getSshKeyName();
		return sshKeys.getOrCreate(sshKeyName, "root");
	}

	public String getSshKeyName() {
		return "service-" + getServiceKey();
	}

	private String getServiceKey() {
		return getServiceType().getKey().toLowerCase();
	}

	private ServiceType getServiceType() {
		return opsContext.getServiceConfiguration().getServiceType();
	}

	public String getSecurityGroupName() {
		return "service-" + getServiceKey();
	}

	public X500Principal getX500Principal() {
		return new X500Principal("CN=" + getServiceKey());
	}

}
