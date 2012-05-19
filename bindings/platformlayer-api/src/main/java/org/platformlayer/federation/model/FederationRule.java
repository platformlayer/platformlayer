package org.platformlayer.federation.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.platformlayer.core.model.PlatformLayerKey;

@XmlAccessorType(XmlAccessType.FIELD)
public class FederationRule {
	public PlatformLayerKey target;
	public String serviceType;
}
