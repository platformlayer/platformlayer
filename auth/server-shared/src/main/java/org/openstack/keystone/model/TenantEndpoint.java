package org.openstack.keystone.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class TenantEndpoint {
    @XmlAttribute
    public String href;

    @XmlAttribute
    public String id;
}
