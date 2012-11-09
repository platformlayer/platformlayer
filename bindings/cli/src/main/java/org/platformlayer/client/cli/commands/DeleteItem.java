package org.platformlayer.client.cli.commands;

import java.io.PrintWriter;

import org.kohsuke.args4j.Argument;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.client.cli.model.ItemPath;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.jobs.model.JobExecutionData;

public class DeleteItem extends PlatformLayerCommandRunnerBase {
	@Argument(index = 0)
	public ItemPath path;

	public DeleteItem() {
		super("delete", "item");
	}

	@Override
	public Object runCommand() throws PlatformLayerClientException {
		PlatformLayerClient client = getPlatformLayerClient();

		PlatformLayerKey key = path.resolve(getContext());

		JobExecutionData jobData = client.deleteItem(key);
		return jobData;
	}

	@Override
	public void formatRaw(Object o, PrintWriter writer) {
		JobExecutionData jobData = (JobExecutionData) o;
		writer.println(jobData.executionId);
	}
}
