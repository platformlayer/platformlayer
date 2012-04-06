package org.platformlayer.service.cloud.direct.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.PublicEndpointBase;
import org.platformlayer.service.cloud.direct.ops.DirectPublicEndpointController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(DirectPublicEndpointController.class)
public class DirectPublicEndpoint extends PublicEndpointBase {
}
