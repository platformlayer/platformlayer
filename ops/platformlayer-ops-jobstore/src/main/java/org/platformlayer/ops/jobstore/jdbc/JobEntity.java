package org.platformlayer.ops.jobstore.jdbc;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.platformlayer.jobs.model.JobState;

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

	@Column(name = "lastrun_ended_at")
	public Date lastrunEndedAt;

	@Column(name = "lastrun_state")
	public JobState lastrunState;

	@Column(name = "lastrun_job_id")
	public String lastrunId;
}
