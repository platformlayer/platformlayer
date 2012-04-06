package org.platformlayer.service.cloud.raw.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.InstanceBase;
import org.platformlayer.service.cloud.raw.ops.RawInstanceController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(RawInstanceController.class)
public class RawInstance extends InstanceBase {
}
