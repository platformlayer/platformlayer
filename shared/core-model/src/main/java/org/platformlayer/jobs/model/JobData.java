package org.platformlayer.jobs.model;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.common.Job;
import org.platformlayer.common.JobState;
import org.platformlayer.core.model.Action;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ids.ItemType;
import org.platformlayer.ids.ManagedItemId;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.ids.ServiceType;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class JobData implements Job {
	public static final ServiceType SERVICE_TYPE = new ServiceType("jobs");
	public static final ItemType ITEM_TYPE = new ItemType("job");

	public PlatformLayerKey key;
	public PlatformLayerKey targetId;
	public Action action;

	public JobState state;

	public JobLog log;

	public Date startedAt;
	public Date endedAt;

	public void setState(JobState state) {
		this.state = state;
	}

	@Override
	public JobState getState() {
		return state;
	}

	public static PlatformLayerKey buildKey(ProjectId projectId, ManagedItemId jobId) {
		return new PlatformLayerKey(null, projectId, JobData.SERVICE_TYPE, JobData.ITEM_TYPE, jobId);
	}

	@Override
	public String getJobId() {
		return key.getItemIdString();
	}

	@Override
	public PlatformLayerKey getTargetKey() {
		return targetId;
	}

	@Override
	public PlatformLayerKey getJobKey() {
		return key;
	}

	@Override
	public Action getAction() {
		return action;
	}

	@Override
	public Date getStartedAt() {
		return startedAt;
	}

	@Override
	public Date getEndedAt() {
		return endedAt;
	}

	@Override
	public JobLog getLog() {
		return log;
	}

}
