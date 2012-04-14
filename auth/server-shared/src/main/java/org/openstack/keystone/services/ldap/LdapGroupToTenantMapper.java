package org.openstack.keystone.services.ldap;

import java.util.List;

import org.openstack.keystone.services.GroupToTenantMapper;
import org.openstack.keystone.services.TenantInfo;

import com.google.common.collect.Lists;

public class LdapGroupToTenantMapper implements GroupToTenantMapper {
	static class LdapMapping {
		String groupSuffix;
		String serviceKey;
	}

	final List<LdapMapping> ldapMappings = Lists.newArrayList();

	@Override
	public TenantInfo mapGroupToTenant(String groupId) {
		for (LdapMapping ldapMapping : ldapMappings) {
			if (!groupId.endsWith(ldapMapping.groupSuffix)) {
				continue;
			}

			// We expect the group to be called something like:
			// dn=Project1 Read, cn=Nova, cn=openstack, cn=org

			// ,cn=Nova,cn=openstack,cn=org is the suffix, and determines the service key

			String prefix = groupId.substring(0, groupId.length() - ldapMapping.groupSuffix.length());

			// The prefix is e.g. "dn=Project1 Read"

			// For now, we simply assume this format...
			int equalsIndex = prefix.indexOf("=");
			if (equalsIndex == -1) {
				continue;
			}
			String[] components = prefix.substring(equalsIndex + 1).split(" ");
			if (components.length != 2) {
				continue;
			}

			TenantInfo tenantInfo = new TenantInfo();
			tenantInfo.serviceKey = ldapMapping.serviceKey;
			tenantInfo.tenantId = components[0];
			tenantInfo.roleId = components[1];

			return tenantInfo;
		}

		return null;
	}

}
