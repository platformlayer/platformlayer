package org.platformlayer.service.federation.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.service.federation.ops.FederatedServiceController;
import org.platformlayer.xaas.Controller;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Controller(FederatedServiceController.class)
public class FederatedService extends ItemBase {
    public String server;

    public String tenant;

    public String username;

    public String secret;
}
