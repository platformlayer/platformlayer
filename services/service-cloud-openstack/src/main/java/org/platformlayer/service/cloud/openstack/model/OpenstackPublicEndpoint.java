package org.platformlayer.service.cloud.openstack.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.PublicEndpointBase;
import org.platformlayer.service.cloud.openstack.ops.OpenstackPublicEndpointController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(OpenstackPublicEndpointController.class)
public class OpenstackPublicEndpoint extends PublicEndpointBase {
}
