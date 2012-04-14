package org.platformlayer.service.openldap.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.codegen.GwtModel;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.Secret;
import org.platformlayer.service.openldap.ops.LdapServiceController;
import org.platformlayer.xaas.Controller;
import org.platformlayer.xaas.SingletonService;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(LdapServiceController.class)
@SingletonService
@GwtModel
public class LdapService extends ItemBase {
	public String dnsName;
	public Secret ldapServerPassword;
}
