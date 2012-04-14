package org.platformlayer.service.zookeeper.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.service.zookeeper.ops.ZookeeperServerController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(ZookeeperServerController.class)
public class ZookeeperServer extends ItemBase {
	public String clusterDnsName;

	public String clusterId;
}
