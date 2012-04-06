package org.platformlayer.service.cloud.openstack.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.MachineCloudBase;
import org.platformlayer.core.model.Secret;
import org.platformlayer.service.cloud.openstack.ops.OpenstackCloudController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(OpenstackCloudController.class)
public class OpenstackCloud extends MachineCloudBase {
    public String endpoint;

    public String username;
    public Secret password;

    public String tenant;
}
