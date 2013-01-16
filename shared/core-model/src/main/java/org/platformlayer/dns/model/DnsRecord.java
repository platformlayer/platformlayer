package org.platformlayer.dns.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.codegen.GwtModel;
import org.platformlayer.core.model.ItemBase;

import com.google.common.collect.Lists;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
// @Controller(DnsRecordController.class)
@GwtModel
public class DnsRecord extends ItemBase {
	public String dnsName;
	public String recordType;
	public List<String> address;

	public String getDnsName() {
		return dnsName;
	}

	public void setDnsName(String dnsName) {
		this.dnsName = dnsName;
	}

	public String getRecordType() {
		return recordType;
	}

	public void setRecordType(String recordType) {
		this.recordType = recordType;
	}

	public List<String> getAddress() {
		if (address == null) {
			address = Lists.newArrayList();
		}
		return address;
	}

	public void setAddress(List<String> address) {
		this.address = address;
	}

}
