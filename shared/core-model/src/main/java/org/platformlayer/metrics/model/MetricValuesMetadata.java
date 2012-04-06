package org.platformlayer.metrics.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import com.google.common.collect.Lists;

/**
 * This is based on RRD's XML export format
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class MetricValuesMetadata {
    public long start;
    public long step;
    public long end;
    public long rows;
    public long columns;

    @XmlElementWrapper(name = "legend")
    @XmlElement(name = "entry")
    public List<String> columnNames = Lists.newArrayList();
}
