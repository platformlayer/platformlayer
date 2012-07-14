package org.platformlayer.auth;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "service_accounts")
public class ServiceAccountEntity implements ServiceAccount {
	@Id
	public String subject;

	@Column(name = "public_key")
	public byte[] publicKeyData;

}
