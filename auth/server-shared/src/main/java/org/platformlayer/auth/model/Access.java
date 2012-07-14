package org.platformlayer.auth.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Access {
	public Token token;

	// public List<Service> serviceCatalog;

	public List<String> projects;
}
