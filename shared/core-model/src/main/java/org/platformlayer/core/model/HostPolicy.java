package org.platformlayer.core.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class HostPolicy {
	public boolean allowRunInContainer = true;

	/**
	 * A string, which identifies a 'group'. The group can drive a placement strategy
	 * 
	 * e.g. "Put machines in the same group as redundantly as possible"
	 * 
	 * e.g. "Not the same machine, but otherwise as close as possible"
	 */
	public String groupId;

	public float scoreSameGroup;

	public float scoreSameItemType;

	public void configureSpread(String placementKey) {
		this.groupId = placementKey;
		this.scoreSameGroup = -100;
	}

	/**
	 * Position near other members of the group, but avoid instances with the same type
	 */
	public void configureCluster(String placementKey) {
		this.groupId = placementKey;
		this.scoreSameGroup = 10;
		this.scoreSameItemType = -100;
	}
}
