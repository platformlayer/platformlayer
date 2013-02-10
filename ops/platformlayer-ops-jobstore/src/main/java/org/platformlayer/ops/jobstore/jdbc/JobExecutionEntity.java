package org.platformlayer.ops.jobstore.jdbc;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.platformlayer.jobs.model.JobState;

@Entity()
@Table(name = "job_execution")
public class JobExecutionEntity {
	@Id
	@Column
	public int project;

	@Id
	@Column(name = "id")
	public String executionId;

	@Column(name = "job_id")
	public String jobId;

	@Column
	public JobState state;

	@Column(name = "started_at")
	public Date startedAt;

	@Column(name = "ended_at")
	public Date endedAt;
}
