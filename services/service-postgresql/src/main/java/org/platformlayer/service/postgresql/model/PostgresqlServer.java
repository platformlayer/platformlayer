package org.platformlayer.service.postgresql.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.Secret;
import org.platformlayer.service.postgresql.ops.PostgresqlServerController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(PostgresqlServerController.class)
public class PostgresqlServer extends ItemBase {
	public String dnsName;

	public Secret rootPassword;
}
