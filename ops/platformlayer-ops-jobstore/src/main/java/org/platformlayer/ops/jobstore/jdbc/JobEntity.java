package org.platformlayer.ops.jobstore.jdbc;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity()
@Table(name = "job")
public class JobEntity {
	@Column
	public int project;
	@Column(name = "id")
	public String jobId;

	@Column(name = "action")
	public String actionXml;

	@Column
	public String target;
}
