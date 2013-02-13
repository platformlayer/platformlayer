package org.platformlayer.core.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

@XmlAccessorType(XmlAccessType.PROPERTY)
public class LinkList {
	@JsonCreator
	public LinkList(List<Link> values) {
		this.links = values;
	}

	public List<Link> links;

	public LinkList() {
	}

	public static LinkList build() {
		LinkList tag = new LinkList();
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

}
