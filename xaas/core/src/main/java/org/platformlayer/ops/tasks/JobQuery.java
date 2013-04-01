package org.platformlayer.ops.tasks;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ids.ProjectId;

import com.fathomdb.TimeSpan;

public class JobQuery {
	public ProjectId project;
	public PlatformLayerKey target;
	public TimeSpan maxAge;
	public Integer limit;

	public static JobQuery build(ProjectId project, PlatformLayerKey filterTarget) {
		JobQuery jobQuery = new JobQuery();
		jobQuery.project = project;
		jobQuery.target = filterTarget;

		if (jobQuery.target == null) {
			jobQuery.maxAge = TimeSpan.FIVE_MINUTES;
		} else {
			jobQuery.limit = 10;
		}

		return jobQuery;
	}
}
