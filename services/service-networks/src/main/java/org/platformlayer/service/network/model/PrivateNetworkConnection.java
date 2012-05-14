package org.platformlayer.service.network.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.service.network.ops.PrivateNetworkConnectionController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(PrivateNetworkConnectionController.class)
public class PrivateNetworkConnection extends ItemBase {
	public PlatformLayerKey network;
	public PlatformLayerKey machine;

	public String cidr;
	public String tunnelId;
}
