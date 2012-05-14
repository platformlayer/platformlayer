package org.platformlayer.service.cloud.direct.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.service.cloud.direct.ops.DirectHostController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(DirectHostController.class)
public class DirectHost extends ItemBase {
	public PlatformLayerKey cloud;
	// public PlatformLayerKey machineSource;

	public String host;

	public String ipv4Public;
	public String ipv4Private;
	public String ipv6;

	public String bridge = "br100";
}
