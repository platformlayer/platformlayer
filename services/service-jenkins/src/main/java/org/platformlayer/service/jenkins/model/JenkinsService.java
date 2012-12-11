package org.platformlayer.service.jenkins.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.codegen.GwtModel;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.service.jenkins.ops.JenkinsServiceController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(JenkinsServiceController.class)
@GwtModel
public class JenkinsService extends ItemBase {
	public String dnsName;
}
