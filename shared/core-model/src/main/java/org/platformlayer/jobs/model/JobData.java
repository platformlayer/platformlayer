package org.platformlayer.jobs.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.Action;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ids.ItemType;
import org.platformlayer.ids.ManagedItemId;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.ids.ServiceType;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class JobData {
	public static final ServiceType SERVICE_TYPE = new ServiceType("jobs");
	public static final ItemType ITEM_TYPE = new ItemType("job");

	public PlatformLayerKey key;
	public PlatformLayerKey targetId;
	public Action action;

	public JobState state;

	public void setState(JobState state) {
		this.state = state;
	}

	public JobState getState() {
		return state;
	}

	public static PlatformLayerKey buildKey(ProjectId projectId, ManagedItemId jobId) {
		return new PlatformLayerKey(null, projectId, JobData.SERVICE_TYPE, JobData.ITEM_TYPE, jobId);
	}
}
