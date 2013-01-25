package org.platformlayer.core.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Joiner;

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
	public List<Property> getProperty() {
		return property;
	}

	public void setProperty(List<Property> property) {
		this.property = property;
	}

}
