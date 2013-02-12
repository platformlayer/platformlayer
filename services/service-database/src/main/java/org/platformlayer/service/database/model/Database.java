package org.platformlayer.service.database.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.Generate;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Secret;
import org.platformlayer.service.database.ops.DatabaseController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(DatabaseController.class)
public class Database extends ItemBase {
	public PlatformLayerKey server;

	public String username;

	@Generate
	public Secret password;

	public String databaseName = "main";
}
