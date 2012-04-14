package org.platformlayer.metrics.model;

import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;

/**
 * This is based on RRD's XML export format
 */
@XmlRootElement(name = "xport")
@XmlAccessorType(XmlAccessType.FIELD)
public class MetricValues implements Iterable<MetricValue> {
	@XmlElementWrapper(name = "data")
	@XmlElement(name = "row")
	public List<MetricValue> rows = Lists.newArrayList();

	public MetricValuesMetadata meta;

	@Override
	public Iterator<MetricValue> iterator() {
		return rows.iterator();
	}
}
