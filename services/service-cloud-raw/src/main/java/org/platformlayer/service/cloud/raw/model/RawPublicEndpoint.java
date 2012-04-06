package org.platformlayer.service.cloud.raw.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.PublicEndpointBase;
import org.platformlayer.service.cloud.raw.ops.RawPublicEndpointController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(RawPublicEndpointController.class)
public class RawPublicEndpoint extends PublicEndpointBase {
}
