package org.platformlayer.service.cloud.direct.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.service.cloud.direct.ops.DirectNetworkAssignmentController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(DirectNetworkAssignmentController.class)
public class DirectNetworkAssignment extends ItemBase {
}
