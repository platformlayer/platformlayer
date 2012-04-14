package org.platformlayer.core.model;

import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceInfoCollection implements Iterable<ServiceInfo> {
	public List<ServiceInfo> services;

	@Override
	public Iterator<ServiceInfo> iterator() {
		return services.iterator();
	}

	@Override
	public String toString() {
		return "ServiceInfoCollection [services=" + services + "]";
	}

}
