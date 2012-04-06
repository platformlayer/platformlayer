package org.platformlayer.core.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceInfo {
    public String serviceType;
    public String namespace;
    public String description;
    public List<String> publicTypes;
    public List<String> adminTypes;

    public String getServiceType() {
        return serviceType;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getPublicTypes() {
        return publicTypes;
    }

    public List<String> getAdminTypes() {
        return adminTypes;
    }

    @Override
    public String toString() {
        return "ServiceInfo [serviceType=" + serviceType + ", namespace=" + namespace + ", description=" + description + ", publicTypes=" + publicTypes + ", adminTypes=" + adminTypes + "]";
    }

    // @Override
    // public String toString() {
    // return JsonHelper.build(ServiceInfo.class).toStringHelper(this);
    // // return JaxbHelper.toStringHelper(this);
    // }

}
