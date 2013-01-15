package org.platformlayer.instances.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.HostPolicy;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;

import com.google.common.collect.Lists;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
// @Controller(PersistentInstanceController.class)
public class PersistentInstance extends ItemBase {
	public PlatformLayerKey recipe;
	public String dnsName;
	public String sshPublicKey;
	public String securityGroup;
	public int minimumRam;

	public PlatformLayerKey cloud;

	public HostPolicy hostPolicy;

	public List<Integer> publicPorts;

	public PlatformLayerKey getRecipe() {
		return recipe;
	}

	public void setRecipe(PlatformLayerKey recipe) {
		this.recipe = recipe;
	}

	public String getDnsName() {
		return dnsName;
	}

	public void setDnsName(String dnsName) {
		this.dnsName = dnsName;
	}

	public String getSshPublicKey() {
		return sshPublicKey;
	}

	public void setSshPublicKey(String sshPublicKey) {
		this.sshPublicKey = sshPublicKey;
	}

	public String getSecurityGroup() {
		return securityGroup;
	}

	public void setSecurityGroup(String securityGroup) {
		this.securityGroup = securityGroup;
	}

	public int getMinimumRam() {
		return minimumRam;
	}

	public void setMinimumRam(int minimumRam) {
		this.minimumRam = minimumRam;
	}

	public PlatformLayerKey getCloud() {
		return cloud;
	}

	public void setCloud(PlatformLayerKey cloud) {
		this.cloud = cloud;
	}

	public HostPolicy getHostPolicy() {
		return hostPolicy;
	}

	public void setHostPolicy(HostPolicy hostPolicy) {
		this.hostPolicy = hostPolicy;
	}

	public List<Integer> getPublicPorts() {
		if (publicPorts == null) {
			publicPorts = Lists.newArrayList();
		}
		return publicPorts;
	}

	public void setPublicPorts(List<Integer> publicPorts) {
		this.publicPorts = publicPorts;
	}

}
