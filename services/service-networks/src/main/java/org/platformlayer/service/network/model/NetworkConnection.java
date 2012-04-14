package org.platformlayer.service.network.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.service.network.ops.NetworkConnectionController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(NetworkConnectionController.class)
public class NetworkConnection extends ItemBase {
	public PlatformLayerKey sourceItem;
	public String sourceCidr;
	public PlatformLayerKey destItem;
	public int port;
	public String protocol;
}
