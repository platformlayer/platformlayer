package org.platformlayer.auth;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.openstack.keystone.services.ServiceAccount;

@Entity
@Table(name = "service_accounts")
public class OpsServiceAccount implements ServiceAccount {
	@Id
	public String subject;

	@Column(name = "public_key")
	public byte[] publicKeyData;

}
