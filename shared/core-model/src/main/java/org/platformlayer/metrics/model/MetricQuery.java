package org.platformlayer.metrics.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.PlatformLayerKey;

import com.google.common.collect.Lists;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class MetricQuery {
	public List<String> filters = Lists.newArrayList();
	public PlatformLayerKey item;

	public MetricQuery copy() {
		MetricQuery copy = new MetricQuery();
		copy.item = this.item;
		copy.filters = Lists.newArrayList(this.filters);
		return copy;
	}
}
