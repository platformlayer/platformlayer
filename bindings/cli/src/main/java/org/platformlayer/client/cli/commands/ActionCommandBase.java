package org.platformlayer.client.cli.commands;

import java.io.PrintWriter;

import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.client.cli.model.ItemPath;
import org.platformlayer.core.model.Action;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.jobs.model.JobExecutionData;

public abstract class ActionCommandBase extends PlatformLayerCommandRunnerBase {

	protected ActionCommandBase(String actionName) {
		super(actionName, "item");
	}

	protected JobExecutionData runAction(ItemPath path, Action action) throws PlatformLayerClientException {
		PlatformLayerClient client = getPlatformLayerClient();

		PlatformLayerKey key = path.resolve(getContext());

		JobExecutionData ret = client.doAction(key, action);
		return ret;
	}

	@Override
	public void formatRaw(Object o, PrintWriter writer) {
		JobExecutionData jobData = (JobExecutionData) o;
		writer.println(jobData.getJobKey().getItemId().getKey() + "/" + jobData.getExecutionId());
	}

}
