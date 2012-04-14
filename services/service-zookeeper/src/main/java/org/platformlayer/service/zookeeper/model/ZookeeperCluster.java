package org.platformlayer.service.zookeeper.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.service.zookeeper.ops.ZookeeperClusterController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(ZookeeperClusterController.class)
public class ZookeeperCluster extends ItemBase {
	public String dnsName;

	public int clusterSize = 3;
}
