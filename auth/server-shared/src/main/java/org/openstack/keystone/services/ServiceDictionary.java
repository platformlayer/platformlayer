package org.openstack.keystone.services;

import org.openstack.keystone.model.Service;

public interface ServiceDictionary {
    Service getServiceInfo(String serviceKey, String tenantId);
}
