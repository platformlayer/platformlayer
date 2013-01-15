package org.platformlayer.client.cli.commands;

import java.io.PrintWriter;

import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.client.cli.model.ItemPath;
import org.platformlayer.core.model.Action;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.jobs.model.JobData;

public abstract class ActionCommandBase extends PlatformLayerCommandRunnerBase {

	protected ActionCommandBase(String actionName) {
		super(actionName, "item");
	}

	protected JobData runAction(ItemPath path, Action action) throws PlatformLayerClientException {
		PlatformLayerClient client = getPlatformLayerClient();

		PlatformLayerKey key = path.resolve(getContext());

		JobData ret = client.doAction(key, action);
		return ret;
	}

	@Override
	public void formatRaw(Object o, PrintWriter writer) {
		JobData jobData = (JobData) o;
		writer.println(jobData.getJobId());
	}

}
