package org.platformlayer.ops.jobstore.jdbc;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity()
@Table(name = "job")
public class JobEntity {
	@Id
	@Column
	public int project;

	@Id
	@Column(name = "id")
	public String jobId;

	@Column(name = "action")
	public String actionXml;

	@Column
	public String target;
}
