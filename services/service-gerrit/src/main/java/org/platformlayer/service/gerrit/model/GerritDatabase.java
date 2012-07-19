package org.platformlayer.service.gerrit.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.Generate;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Secret;
import org.platformlayer.service.gerrit.ops.db.GerritDatabaseController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(GerritDatabaseController.class)
public class GerritDatabase extends ItemBase {
	public PlatformLayerKey server;

	@Generate("gerrit2")
	public String username;

	@Generate
	public Secret password;

	@Generate("gerrit2")
	public String databaseName;
}
