package org.platformlayer.client.cli.commands;

import java.io.PrintWriter;

import org.kohsuke.args4j.Argument;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.client.cli.model.CliAction;
import org.platformlayer.client.cli.model.ItemPath;
import org.platformlayer.core.model.Action;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.jobs.model.JobData;

public class DoAction extends PlatformLayerCommandRunnerBase {
	@Argument(index = 0, usage = "path", required = true)
	public ItemPath path;

	@Argument(index = 1, usage = "action", required = true)
	public CliAction action;

	public DoAction() {
		super("do", "action");
	}

	@Override
	public Object runCommand() throws PlatformLayerClientException {
		PlatformLayerClient client = getPlatformLayerClient();

		PlatformLayerKey key = path.resolve(getContext());

		Action actionCommand = new Action(action.getKey());

		JobData ret = client.doAction(key, actionCommand);
		return ret;
	}

	@Override
	public void formatRaw(Object o, PrintWriter writer) {
		JobData jobData = (JobData) o;
		writer.println(jobData.key);
	}

}
