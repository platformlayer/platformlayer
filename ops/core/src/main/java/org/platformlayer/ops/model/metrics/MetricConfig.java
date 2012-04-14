package org.platformlayer.ops.model.metrics;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class MetricConfig {
	@XmlAttribute(required = true)
	public String key;

	public List<MetricConfig> metrics;
	public List<Metric> metric;
}
