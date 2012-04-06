package org.openstack.keystone.model;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class Token {
    public Date expires;
    public String id;
    public Tenant tenant;
}
