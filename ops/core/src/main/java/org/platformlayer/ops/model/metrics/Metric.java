package org.platformlayer.ops.model.metrics;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class Metric {
	@XmlAttribute(required = true)
	public String key;

	@XmlAttribute
	public String rrd;
}
