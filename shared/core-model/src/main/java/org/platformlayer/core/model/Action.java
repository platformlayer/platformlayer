package org.platformlayer.core.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Strings;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class Action {
	public Action(String name) {
		this.name = name;
		if (Strings.isNullOrEmpty(name)) {
			throw new IllegalArgumentException();
		}
	}

	public Action() {
	}

	public String name;

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "Action [name=" + name + "]";
	}
}
