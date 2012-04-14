package org.platformlayer.service.imagefactory.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class Repository {
	public String key;

	public List<String> source;
}
