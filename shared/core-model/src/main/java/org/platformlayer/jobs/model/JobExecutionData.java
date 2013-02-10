package org.platformlayer.jobs.model;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.PlatformLayerKey;

@XmlRootElement(name = "jobExecution")
@XmlAccessorType(XmlAccessType.FIELD)
public class JobExecutionData implements IsJobExecution {
	public PlatformLayerKey jobKey;
	public String executionId;

	public JobState state;

	public Date startedAt;
	public Date endedAt;

	// Optional
	public JobData job;

	public void setState(JobState state) {
		this.state = state;
	}

	@Override
	public JobState getState() {
		return state;
	}

	@Override
	public PlatformLayerKey getJobKey() {
		return jobKey;
	}

	@Override
	public Date getStartedAt() {
		return startedAt;
	}

	@Override
	public Date getEndedAt() {
		return endedAt;
	}

	public JobData getJob() {
		return job;
	}

	public String getExecutionId() {
		return executionId;
	}

	public void setJob(JobData job) {
		this.job = job;
	}

	public String getJobId() {
		return jobKey.getItemIdString();
	}
}
