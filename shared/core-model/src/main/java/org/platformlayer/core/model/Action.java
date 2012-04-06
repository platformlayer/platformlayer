package org.platformlayer.core.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class Action {
    public Action(String name) {
        this.name = name;
    }

    public Action() {
    }

    public String name;

    public String getName() {
        return name;
    }
}
