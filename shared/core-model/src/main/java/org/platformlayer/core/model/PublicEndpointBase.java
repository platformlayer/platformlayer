package org.platformlayer.core.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class PublicEndpointBase extends ItemBase {
	// public String network;
	public int publicPort;
	public PlatformLayerKey instance;
	public int backendPort;
}
