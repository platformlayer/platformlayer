package org.platformlayer.ops.schedule;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.platformlayer.core.model.Action;
import org.platformlayer.core.model.PlatformLayerKey;

@XmlAccessorType(XmlAccessType.FIELD)
public class ActionTask extends Task {
	public EndpointRecord endpoint;
	public PlatformLayerKey target;
	public Action action;
}
