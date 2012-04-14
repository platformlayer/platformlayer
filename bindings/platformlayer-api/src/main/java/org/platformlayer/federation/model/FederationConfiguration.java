package org.platformlayer.federation.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "federation")
public class FederationConfiguration {
	@XmlElement(name = "rule")
	public List<FederationRule> rules = Lists.newArrayList();

	@XmlElement(name = "system")
	public List<PlatformLayerConnectionConfiguration> systems = Lists.newArrayList();
}
