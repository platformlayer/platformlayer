package org.platformlayer.federation.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PlatformLayerConnectionConfiguration {
	public String key;

	public String tenant;

	public String server;

	public String username;

	public String secret;
}
