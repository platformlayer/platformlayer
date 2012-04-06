package org.openstack.keystone.services.memory;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

import org.openstack.keystone.model.Service;
import org.openstack.keystone.model.ServiceEndpoint;
import org.openstack.keystone.model.ServiceVersion;
import org.openstack.keystone.services.ServiceDictionary;
import org.platformlayer.IoUtils;
import org.platformlayer.xml.JsonHelper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class SimpleServiceDictionary implements ServiceDictionary {
    final Map<String, Service> services = Maps.newHashMap();

    public void addService(String serviceKey, Service serviceTemplate) {
        services.put(serviceKey, serviceTemplate);
    }

    @Override
    public Service getServiceInfo(String serviceKey, String tenantId) {
        Service service = services.get(serviceKey);
        if (service == null)
            return null;

        return cloneAndReplace(service, tenantId);
    }

    private Service cloneAndReplace(Service service, String tenantId) {
        Service clone = new Service();
        clone.endpoints = cloneAndReplace(service.endpoints, tenantId);
        clone.name = cloneAndReplace(service.name, tenantId);
        clone.type = cloneAndReplace(service.type, tenantId);
        return clone;
    }

    private List<ServiceEndpoint> cloneAndReplace(List<ServiceEndpoint> endpoints, String tenantId) {
        if (endpoints == null)
            return null;
        List<ServiceEndpoint> clone = Lists.newArrayList();
        for (ServiceEndpoint endpoint : endpoints)
            clone.add(cloneAndReplace(endpoint, tenantId));
        return clone;
    }

    private ServiceEndpoint cloneAndReplace(ServiceEndpoint endpoint, String tenantId) {
        ServiceEndpoint clone = new ServiceEndpoint();
        clone.region = cloneAndReplace(endpoint.region, tenantId);
        clone.tenantId = cloneAndReplace(endpoint.tenantId, tenantId);
        clone.internalURL = cloneAndReplace(endpoint.internalURL, tenantId);
        clone.publicURL = cloneAndReplace(endpoint.publicURL, tenantId);
        clone.version = cloneAndReplace(endpoint.version, tenantId);

        return clone;
    }

    private ServiceVersion cloneAndReplace(ServiceVersion version, String tenantId) {
        if (version == null)
            return null;

        ServiceVersion clone = new ServiceVersion();
        clone.id = cloneAndReplace(version.id, tenantId);
        clone.info = cloneAndReplace(version.info, tenantId);
        clone.list = cloneAndReplace(version.list, tenantId);

        return clone;

    }

    private String cloneAndReplace(String s, String tenantId) {
        if (s == null)
            return null;

        if (s.contains("{tenantId}")) {
            s = s.replace("{tenantId}", tenantId);
        }

        return s;
    }

    public static SimpleServiceDictionary loadFromDirectory(File dir) {
        // JaxbHelper jaxbHelper = JaxbHelper.get(Service.class);
        JsonHelper<Service> json = JsonHelper.build(Service.class);
        json.addDefaultNamespace();

        SimpleServiceDictionary dictionary = new SimpleServiceDictionary();

        for (File file : dir.listFiles()) {
            String serviceKey = file.getName();

            Service serviceTemplate;
            try {
                FileInputStream fis = new FileInputStream(file);
                try {
                    serviceTemplate = json.unmarshal(fis);
                    // serviceTemplate = (Service) jaxbHelper.unmarshal(fis, Service.class);
                } finally {
                    IoUtils.safeClose(fis);
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Error loading file: " + file, e);
            }

            dictionary.addService(serviceKey, serviceTemplate);
        }

        return dictionary;
    }
}
