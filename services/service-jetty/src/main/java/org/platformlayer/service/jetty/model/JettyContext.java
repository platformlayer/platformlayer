package org.platformlayer.service.jetty.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.platformlayer.codegen.GwtModel;
import org.platformlayer.core.model.Links;

@XmlAccessorType(XmlAccessType.FIELD)
@GwtModel
public class JettyContext {
	public String id;
	public String source;

	public Links links;
}
