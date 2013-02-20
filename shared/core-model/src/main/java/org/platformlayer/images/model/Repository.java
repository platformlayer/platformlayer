package org.platformlayer.images.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.google.common.collect.Lists;

@XmlAccessorType(XmlAccessType.FIELD)
public class Repository {
	public String key;

	public List<String> source;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public List<String> getSource() {
		if (source == null) {
			source = Lists.newArrayList();
		}
		return source;
	}

	public void setSource(List<String> source) {
		this.source = source;
	}

}
