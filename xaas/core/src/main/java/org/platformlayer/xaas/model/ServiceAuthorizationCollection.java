package org.platformlayer.xaas.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceAuthorizationCollection {
	@XmlElementWrapper(name = "items")
	@XmlElement(name = "item")
	public List<ServiceAuthorization> items;

}
