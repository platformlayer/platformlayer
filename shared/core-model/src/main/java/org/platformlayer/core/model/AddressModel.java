package org.platformlayer.core.model;

import java.net.InetAddress;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.google.common.net.InetAddresses;

@XmlAccessorType(XmlAccessType.FIELD)
public class AddressModel {
	public String cidr;
	public String gateway;
	public String netmask;
	public String address;
	public String protocol;

	public String getNetmask() {
		return netmask;
	}

	public String getCidr() {
		return cidr;
	}

	public String getAddress() {
		return address;
	}

	public InetAddress getInetAddress() {
		return InetAddresses.forString(getAddress());
	}

	public String getGateway() {
		return gateway;
	}

	public String getProtocol() {
		return protocol;
	}

	public Tag toTag() {
		return Tag.NETWORK_ADDRESS.build(getInetAddress());
	}

	public void copyFrom(AddressModel b) {
		this.cidr = b.cidr;
		this.gateway = b.gateway;
		this.netmask = b.netmask;
		this.address = b.address;
		this.protocol = b.protocol;
	}
}
