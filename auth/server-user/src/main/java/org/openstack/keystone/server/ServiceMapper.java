package org.openstack.keystone.server;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.openstack.keystone.model.Service;
import org.openstack.keystone.services.GroupToTenantMapper;
import org.openstack.keystone.services.ServiceDictionary;
import org.openstack.keystone.services.TenantInfo;
import org.openstack.keystone.services.UserInfo;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class ServiceMapper {
	static final Logger log = Logger.getLogger(ServiceMapper.class);

	@Inject
	ServiceDictionary serviceDictionary;

	@Inject
	GroupToTenantMapper groupToTenantMapper;

	public List<Service> getServices(UserInfo userInfo, String filterTenantId) {
		Collection<String> groups = userInfo.groups;
		List<Service> services = Lists.newArrayList();
		for (String group : groups) {
			TenantInfo tenantInfo = groupToTenantMapper.mapGroupToTenant(group);
			if (tenantInfo != null) {
				if (filterTenantId != null && !Objects.equal(filterTenantId, tenantInfo.tenantId)) {
					continue;
				}

				Service service = serviceDictionary.getServiceInfo(tenantInfo.serviceKey, tenantInfo.tenantId);
				if (service == null) {
					log.warn("Could not resolve service: " + tenantInfo.serviceKey);
				} else {
					services.add(service);
				}
			}
		}

		return services;
	}
}
