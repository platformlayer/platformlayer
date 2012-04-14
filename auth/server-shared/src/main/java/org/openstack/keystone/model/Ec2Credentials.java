package org.openstack.keystone.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class Ec2Credentials {
	public String access;
	public String signature;
	public String host;
	public String verb;
	public String path;
	public Ec2RequestParameters params;
}
