package org.openstack.keystone.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class Role {
    public String name;

    public String tenantId;

    public String description;
}
