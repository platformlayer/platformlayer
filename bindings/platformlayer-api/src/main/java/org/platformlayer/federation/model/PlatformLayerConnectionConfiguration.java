package org.platformlayer.federation.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.PlatformLayerKey;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PlatformLayerConnectionConfiguration {
	public PlatformLayerKey key;

	public String tenant;

	public String authenticationEndpoint;

	public String username;

	public String secret;

	public String platformlayerEndpoint;

	public List<String> authTrustKeys;
	public List<String> platformlayerTrustKeys;
}
