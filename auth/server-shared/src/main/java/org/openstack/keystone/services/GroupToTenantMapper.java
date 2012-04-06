package org.openstack.keystone.services;

public interface GroupToTenantMapper {
    TenantInfo mapGroupToTenant(String groupId);
}
