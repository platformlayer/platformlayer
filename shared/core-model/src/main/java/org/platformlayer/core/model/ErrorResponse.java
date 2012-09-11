package org.platformlayer.core.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class ErrorResponse {
	public String message;
	public String code;

	public List<ErrorDetail> details = Lists.newArrayList();
}
