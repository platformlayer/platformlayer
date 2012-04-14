package org.openstack.service.nginx.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.openstack.service.nginx.ops.NginxServiceController;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(NginxServiceController.class)
public class NginxService extends ItemBase {
	public String dnsName;
}
