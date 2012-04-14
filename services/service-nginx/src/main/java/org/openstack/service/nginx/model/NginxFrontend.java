package org.openstack.service.nginx.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.openstack.service.nginx.ops.NginxFrontendController;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(NginxFrontendController.class)
public class NginxFrontend extends ItemBase {
	public String hostname;
}
