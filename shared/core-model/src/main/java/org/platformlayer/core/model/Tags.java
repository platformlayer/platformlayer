package org.platformlayer.core.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.common.IsTag;
import org.platformlayer.common.TagsetBase;

import com.google.common.collect.Lists;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class Tags extends TagsetBase {
	public List<IsTag> tags = Lists.newArrayList();

	@Override
	public List<IsTag> getTags() {
		return tags;
	}
}
