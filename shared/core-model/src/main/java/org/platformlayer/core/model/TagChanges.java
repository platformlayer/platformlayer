package org.platformlayer.core.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class TagChanges {
	public Tags addTags = Tags.build();
	public Tags removeTags = Tags.build();

	public boolean isEmpty() {
		return addTags.isEmpty() && removeTags.isEmpty();
	}
}
