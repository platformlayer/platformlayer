package org.platformlayer.service.cloud.google.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.PublicEndpointBase;
import org.platformlayer.service.cloud.google.ops.GoogleCloudPublicEndpointController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(GoogleCloudPublicEndpointController.class)
public class GoogleCloudPublicEndpoint extends PublicEndpointBase {
}
