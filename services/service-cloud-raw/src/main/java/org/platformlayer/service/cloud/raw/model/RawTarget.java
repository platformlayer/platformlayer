package org.platformlayer.service.cloud.raw.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.service.cloud.raw.ops.RawTargetController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(RawTargetController.class)
public class RawTarget extends ItemBase {
	public PlatformLayerKey cloud;
	public String host;
}
