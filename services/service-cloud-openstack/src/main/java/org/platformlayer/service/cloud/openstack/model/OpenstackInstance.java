package org.platformlayer.service.cloud.openstack.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.InstanceBase;
import org.platformlayer.service.cloud.openstack.ops.OpenstackInstanceController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(OpenstackInstanceController.class)
public class OpenstackInstance extends InstanceBase {
	public String hostname;
	public int minimumMemoryMb;
}
