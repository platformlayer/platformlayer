package org.platformlayer.service.cloud.google.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.MachineCloudBase;
import org.platformlayer.core.model.Secret;
import org.platformlayer.service.cloud.google.ops.GoogleCloudController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(GoogleCloudController.class)
public class GoogleCloud extends MachineCloudBase {
	public Secret serviceAccountKey;
	public String serviceAccountId;
	public String projectId;
}
