package org.platformlayer.service.platformlayer.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.Generate;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Secret;
import org.platformlayer.service.platformlayer.ops.backend.db.PlatformLayerDatabaseController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(PlatformLayerDatabaseController.class)
public class PlatformLayerDatabase extends ItemBase {
	public PlatformLayerKey server;

	@Generate("platformlayer_ops")
	public String username;

	@Generate
	public Secret password;

	@Generate("platformlayer_ops")
	public String databaseName;
}
