package org.platformlayer.service.gerrit.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.Generate;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Secret;
import org.platformlayer.service.gerrit.ops.GerritServiceController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(GerritServiceController.class)
public class GerritService extends ItemBase {
	public String dnsName;

	public PlatformLayerKey database;

	@Generate
	public Secret tokenSecret;

	public PlatformLayerKey sslKey;
}
