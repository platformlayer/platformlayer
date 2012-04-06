package org.platformlayer.metrics.model;

import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class MetricInfoCollection implements Iterable<MetricInfo> {
    @XmlElementWrapper(name = "metricInfos")
    @XmlElement(name = "metricInfo")
    public List<MetricInfo> metricInfoList = Lists.newArrayList();

    @Override
    public Iterator<MetricInfo> iterator() {
        return metricInfoList.iterator();
    }

}
