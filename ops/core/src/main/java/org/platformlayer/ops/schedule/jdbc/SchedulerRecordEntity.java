package org.platformlayer.ops.schedule.jdbc;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "scheduler_job")
public class SchedulerRecordEntity {
	@Column
	@Id
	public String key;

	@Column(name = "schedule_interval")
	public String scheduleInterval;

	@Column(name = "schedule_base")
	public Date scheduleBase;

	@Column(name = "task_target")
	public String taskTarget;

	@Column(name = "task_endpoint_url")
	public String taskEndpointUrl;

	@Column(name = "task_endpoint_project")
	public String taskEndpointProject;

	@Column(name = "task_endpoint_secret")
	public byte[] taskEndpointSecret;

	@Column(name = "task_endpoint_token")
	public String taskEndpointToken;

	@Column(name = "task_endpoint_keys")
	public String taskEndpointTrustKeys;

	@Column(name = "task_action")
	public String taskAction;
}
