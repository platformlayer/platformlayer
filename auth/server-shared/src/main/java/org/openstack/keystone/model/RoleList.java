package org.openstack.keystone.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "roles")
@XmlAccessorType(XmlAccessType.FIELD)
public class RoleList {
	@XmlElement(name = "role")
	public List<Role> roles;
}
