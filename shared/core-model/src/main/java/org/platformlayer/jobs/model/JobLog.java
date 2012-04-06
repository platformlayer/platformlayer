package org.platformlayer.jobs.model;

import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class JobLog implements Iterable<JobLogLine> {
    public List<JobLogLine> lines = Lists.newArrayList();

    public Iterable<JobLogLine> getLines() {
        return lines;
    }

    @Override
    public Iterator<JobLogLine> iterator() {
        return lines.iterator();
    }
}
