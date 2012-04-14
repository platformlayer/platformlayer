package org.platformlayer.guice.xaas;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "items")
public class ItemEntity {
	@Id
	public int id;

	@Column
	public int service;

	@Column
	public int model;

	@Column
	public String key;

	@Column
	public int state;

	@Column
	public byte[] data;

	@Column
	public byte[] secret;
}
