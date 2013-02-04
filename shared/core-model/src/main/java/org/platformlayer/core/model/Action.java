package org.platformlayer.core.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.common.IsAction;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class Action implements IsAction {
	// public Action(String name) {
	// this.name = name;
	// if (Strings.isNullOrEmpty(name)) {
	// throw new IllegalArgumentException();
	// }
	// }

	public Action() {
		this.type = getClass().getSimpleName();
	}

	public String type;

	// public String getName() {
	// return name;
	// }

	// @Override
	// public String toString() {
	// return "Action [name=" + name + "]";
	// }

	@Override
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
