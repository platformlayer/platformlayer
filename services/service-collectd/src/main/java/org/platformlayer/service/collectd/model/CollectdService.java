package org.platformlayer.service.collectd.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.service.collectd.ops.CollectdServiceController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(CollectdServiceController.class)
public class CollectdService extends ItemBase {
    public String dnsName;
}
