package org.platformlayer.core.model;

import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ManagedItemCollection<T> implements Iterable<T> {
    @XmlElementWrapper(name = "items")
    @XmlElement(name = "item")
    public List<T> items;

    @Override
    public Iterator<T> iterator() {
        return items.iterator();
    }
}
