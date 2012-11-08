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
	public List<String> projections = Lists.newArrayList();
	public PlatformLayerKey item;

	public MetricQuery copy() {
		MetricQuery copy = new MetricQuery();
		copy.item = this.item;
		copy.filters = Lists.newArrayList(this.filters);
		copy.projections = Lists.newArrayList(this.projections);
		return copy;
	}

	public static MetricQuery create() {
		return new MetricQuery();
	}

	public void setItem(PlatformLayerKey item) {
		this.item = item;
	}

	public void setFilters(List<String> filters) {
		this.filters = filters;
	}

	public void setProjections(List<String> projections) {
		this.projections = projections;
	}
}
