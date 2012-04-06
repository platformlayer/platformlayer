package org.openstack.service.nginx.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.openstack.service.nginx.ops.NginxBackendController;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(NginxBackendController.class)
public class NginxBackend extends ItemBase {
    public String hostname;
    public PlatformLayerKey backend;
}
