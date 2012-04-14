package org.openstack.keystone.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "tenants")
@XmlAccessorType(XmlAccessType.FIELD)
public class TenantEndpointList {
	public List<Tenant> tenants;
}
