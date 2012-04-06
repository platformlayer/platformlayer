package org.openstack.keystone.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceEndpoint {
    public String region;

    public String tenantId;

    public String internalURL;

    public String publicURL;

    public ServiceVersion version;

    @Override
    public String toString() {
        return "ServiceEndpoint [region=" + region + ", tenantId=" + tenantId + ", internalURL=" + internalURL + ", publicURL=" + publicURL + ", version=" + version + "]";
    }
}
