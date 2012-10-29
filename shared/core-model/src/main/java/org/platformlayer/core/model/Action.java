package org.platformlayer.core.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public abstract class Action {
	// public Action(String name) {
	// this.name = name;
	// if (Strings.isNullOrEmpty(name)) {
	// throw new IllegalArgumentException();
	// }
	// }

	public Action() {
		this.type = getType();
	}

	public String type;

	// public String getName() {
	// return name;
	// }

	// @Override
	// public String toString() {
	// return "Action [name=" + name + "]";
	// }

	public String getType() {
		return getClass().getSimpleName();
	}

}
