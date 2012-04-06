package org.platformlayer.service.federation.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.service.federation.ops.FederatedServiceMapController;
import org.platformlayer.xaas.Controller;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Controller(FederatedServiceMapController.class)
public class FederatedServiceMap extends ItemBase {
    public String target;

    public String serviceType;
}
