package org.platformlayer.service.platformlayer.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.service.platformlayer.ops.auth.user.UserAuthServiceController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(UserAuthServiceController.class)
public class UserAuthService extends ItemBase {
	public String dnsName;

	public PlatformLayerKey database;
}
