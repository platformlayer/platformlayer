package org.platformlayer.service.instancesupervisor.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.HostPolicy;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.service.instancesupervisor.ops.PersistentInstanceController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(PersistentInstanceController.class)
public class PersistentInstance extends ItemBase {
	public PlatformLayerKey recipe;
	public String dnsName;
	public String sshPublicKey;
	public String securityGroup;
	public int minimumRam;

	public PlatformLayerKey cloud;

	public HostPolicy hostPolicy;

	public List<Integer> publicPorts;
}
