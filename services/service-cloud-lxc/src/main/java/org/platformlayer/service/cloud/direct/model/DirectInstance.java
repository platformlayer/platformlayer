package org.platformlayer.service.cloud.direct.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.InstanceBase;
import org.platformlayer.service.cloud.direct.ops.DirectInstanceController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(DirectInstanceController.class)
public class DirectInstance extends InstanceBase {
	public String hostname;
	public int minimumMemoryMb;
}
