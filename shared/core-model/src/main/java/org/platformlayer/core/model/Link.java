package org.platformlayer.core.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class Link {
	public String name;
	public PlatformLayerKey target;

	public Link() {
	}

	public static Link build(String name, PlatformLayerKey target) {
		Link tag = new Link();
		tag.name = name;
		tag.target = target;
		return tag;
	}

	public String getName() {
		return name;
	}

	public PlatformLayerKey getTarget() {
		return target;
	}

	@Override
	public String toString() {
		return "Link [name=" + name + ", target=" + target + "]";
	}

}
