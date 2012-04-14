package org.platformlayer.ops;

import javax.inject.Inject;

import org.platformlayer.ResourceUtils;
import org.platformlayer.ops.helpers.ServiceContext;
import org.platformlayer.ops.service.Firewall;
import org.platformlayer.ops.service.Security;

import com.google.common.base.Strings;

public class ServiceInitializer {
	@Inject
	ServiceContext service;

	@Inject
	CloudContext cloud;

	private void applySecurity(Security security) throws OpsException {
		String securityGroup = service.getSecurityGroupName();
		cloud.ensureCreatedSecurityGroup(securityGroup);

		if (security.firewall != null) {
			for (Firewall firewall : security.firewall) {
				int port = firewall.port;
				String protocol = firewall.protocol;
				if (Strings.isNullOrEmpty(protocol)) {
					protocol = "tcp";
				}
				cloud.ensurePortOpen(securityGroup, protocol, port);
			}
		}
	}

	public void initialize(ServiceProviderBase serviceProvider) throws OpsException {
		Security security = ResourceUtils.findResource(serviceProvider.getClass(), "security", Security.class);
		if (security != null) {
			applySecurity(security);
		}
	}
}
