package org.openstack.keystone.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class Tenant {
	@XmlAttribute
	public boolean enabled;

	@XmlAttribute
	public String id;

	@XmlAttribute
	public String name;

	public String description;
}
