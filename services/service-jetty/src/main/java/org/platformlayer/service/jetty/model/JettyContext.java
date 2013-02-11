package org.platformlayer.service.jetty.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.platformlayer.codegen.GwtModel;
import org.platformlayer.core.model.PlatformLayerKey;

@XmlAccessorType(XmlAccessType.FIELD)
@GwtModel
public class JettyContext {
	public String id;
	public String source;

	public List<PlatformLayerKey> use;
}
