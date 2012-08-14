package org.platformlayer.auth;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "user_cert")
public class UserCertEntity {
	@Id
	public int id;

	@Column(name = "user_id")
	public int userId;

	@Column(name = "public_key_hash")
	public byte[] publicKeyHash;

}
