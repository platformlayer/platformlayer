package org.platformlayer.ops.schedule;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.platformlayer.core.model.Secret;

@XmlAccessorType(XmlAccessType.FIELD)
public class EndpointRecord {
	public String url;
	public String project;
	public String token;
	public Secret secret;
	public String trustKeys;
}
