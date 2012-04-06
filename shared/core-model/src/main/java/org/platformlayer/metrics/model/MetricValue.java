package org.platformlayer.metrics.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is based on RRD's XML export format
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class MetricValue {
    @XmlElement(name = "t")
    public long time;

    @XmlElement(name = "v")
    public float value;

    public long getTime() {
        return time;
    }

    public float getValue() {
        return value;
    }
}
