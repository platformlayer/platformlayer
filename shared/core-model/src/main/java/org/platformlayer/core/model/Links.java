package org.platformlayer.core.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlRootElement
public class Links {
	@JsonCreator
	public Links(List<Link> values) {
		this.links = values;
	}

	public List<Link> links;

	public Links() {
	}

	public static Links build() {
		Links tag = new Links();
		return tag;
	}

	@Override
	public String toString() {
		return "Links [" + Joiner.on(",").join(links) + "]";
	}

	@JsonValue
	@XmlElement(name = "link")
	public List<Link> getLinks() {
		if (links == null) {
			links = Lists.newArrayList();
		}
		return links;
	}

	public void setLinks(List<Link> links) {
		this.links = links;
	}

	public Link findLink(String name) {
		for (Link link : getLinks()) {
			if (name.equals(link.getName())) {
				return link;
			}
		}
		return null;
	}

}
