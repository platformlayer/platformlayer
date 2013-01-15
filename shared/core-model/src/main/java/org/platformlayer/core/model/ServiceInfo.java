package org.platformlayer.core.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceInfo {
	public String serviceType;
	public String namespace;
	public String description;
	public List<String> itemTypes;

	public String getServiceType() {
		return serviceType;
	}

	public String getNamespace() {
		return namespace;
	}

	public String getDescription() {
		return description;
	}

	public List<String> getItemTypes() {
		if (itemTypes == null) {
			itemTypes = Lists.newArrayList();
		}
		return itemTypes;
	}

	@Override
	public String toString() {
		return "ServiceInfo [serviceType=" + serviceType + ", namespace=" + namespace + ", description=" + description
				+ ", allTypes=" + itemTypes + "]";
	}

	// @Override
	// public String toString() {
	// return JsonHelper.build(ServiceInfo.class).toStringHelper(this);
	// // return JaxbHelper.toStringHelper(this);
	// }

}
