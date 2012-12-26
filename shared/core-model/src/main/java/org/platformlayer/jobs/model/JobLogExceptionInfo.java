package org.platformlayer.jobs.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.google.common.collect.Lists;

@XmlAccessorType(XmlAccessType.FIELD)
public class JobLogExceptionInfo {
	public List<String> info;

	public JobLogExceptionInfo inner;

	public JobLogExceptionInfo(String[] info) {
		this.info = Lists.newArrayList(info);
	}

	public JobLogExceptionInfo() {
	}

	public List<String> getInfo() {
		if (info == null) {
			info = Lists.newArrayList();
		}
		return info;
	}

	public JobLogExceptionInfo getInner() {
		return inner;
	}

}
