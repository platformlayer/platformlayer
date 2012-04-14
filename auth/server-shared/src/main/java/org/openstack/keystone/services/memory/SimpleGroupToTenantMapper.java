package org.openstack.keystone.services.memory;

import org.openstack.keystone.services.GroupToTenantMapper;
import org.openstack.keystone.services.TenantInfo;

public class SimpleGroupToTenantMapper implements GroupToTenantMapper {

	@Override
	public TenantInfo mapGroupToTenant(String groupId) {
		if (groupId == null) {
			return null;
		}
		String[] components = groupId.split("\\.");
		if (components.length == 3) {
			TenantInfo tenantInfo = new TenantInfo();
			tenantInfo.serviceKey = components[0];
			tenantInfo.tenantId = components[1];
			tenantInfo.roleId = components[2];

			// if (Objects.equal("*", tenantInfo.tenantId)) {
			// tenantInfo.tenantId = null;
			// }

			return tenantInfo;
		}

		if (components.length == 1) {
			TenantInfo tenantInfo = new TenantInfo();
			tenantInfo.serviceKey = "platformlayer";
			tenantInfo.tenantId = components[0];
			tenantInfo.roleId = "admin";

			return tenantInfo;
		}

		return null;
	}

}
