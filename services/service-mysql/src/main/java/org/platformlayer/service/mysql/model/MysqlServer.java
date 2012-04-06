package org.platformlayer.service.mysql.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.Secret;
import org.platformlayer.service.mysql.ops.MysqlServerController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(MysqlServerController.class)
public class MysqlServer extends ItemBase {
    public String dnsName;
    public Secret rootPassword;
}
