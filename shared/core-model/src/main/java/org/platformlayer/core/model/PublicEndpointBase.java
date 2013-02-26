package org.platformlayer.core.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class PublicEndpointBase extends ItemBase {
	// public String network;
	public int publicPort;

	// If this is specified, these ports will all end up on the same public IP address
	// (useful e.g. for port 80 & 443 for SSL)
	public List<Integer> publicPortCluster;

	public PlatformLayerKey instance;
	public int backendPort;

	public String transport;
}
