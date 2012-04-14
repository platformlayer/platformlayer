package org.platformlayer.service.dns.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.codegen.GwtModel;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.service.dns.ops.DnsServerController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(DnsServerController.class)
@GwtModel
public class DnsServer extends ItemBase {
	public String dnsName;
}
