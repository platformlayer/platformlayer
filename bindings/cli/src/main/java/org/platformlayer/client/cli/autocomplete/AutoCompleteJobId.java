package org.platformlayer.client.cli.autocomplete;

import java.util.List;

import org.platformlayer.PlatformLayerClient;
import org.platformlayer.jobs.model.JobData;

import com.fathomdb.cli.CliContext;
import com.google.common.collect.Lists;

public class AutoCompleteJobId extends PlatformLayerSimpleAutoCompleter {
	@Override
	public List<String> doComplete(CliContext context, String prefix) throws Exception {
		PlatformLayerClient client = getPlatformLayerClient(context);
		List<String> jobs = Lists.newArrayList();
		for (JobData jobData : client.listJobs().getJobs()) {
			jobs.add(jobData.getJobId());
		}
		addSuffix(jobs, " ");
		return jobs;
	}
}
