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
public class PropertyList {
	@JsonCreator
	public PropertyList(List<Property> values) {
		this.property = values;
	}

	public List<Property> property;

	public PropertyList() {
	}

	public static PropertyList build() {
		PropertyList tag = new PropertyList();
		return tag;
	}

	@Override
	public String toString() {
		return "Properties [" + Joiner.on(",").join(property) + "]";
	}

	@JsonValue
	@XmlElement(name = "property")
	public List<Property> getProperties() {
		if (property == null) {
			property = Lists.newArrayList();
		}
		return property;
	}

	public void setProperties(List<Property> property) {
		this.property = property;
	}

}
