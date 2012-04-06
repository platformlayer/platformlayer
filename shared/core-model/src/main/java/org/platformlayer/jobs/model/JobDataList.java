package org.platformlayer.jobs.model;

import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Iterators;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class JobDataList implements Iterable<JobData> {
    public List<JobData> jobs;

    @Override
    public Iterator<JobData> iterator() {
        if (jobs == null) {
            return Iterators.emptyIterator();
        }
        return jobs.iterator();
    }
}
