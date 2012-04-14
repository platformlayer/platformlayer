package org.platformlayer.service.openldap.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.codegen.GwtModel;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.Secret;
import org.platformlayer.service.openldap.ops.LdapDomainController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(LdapDomainController.class)
@GwtModel
public class LdapDomain extends ItemBase {
	public String organizationName;
	public Secret adminPassword;
}
