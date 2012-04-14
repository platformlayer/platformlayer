package org.platformlayer.service.wordpress.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Secret;
import org.platformlayer.service.wordpress.ops.WordpressServiceController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(WordpressServiceController.class)
public class WordpressService extends ItemBase {
	public String dnsName;

	public Secret adminPassword;
	public Secret wordpressSecretKey;

	public Secret databasePassword;

	public PlatformLayerKey databaseItem;
}
