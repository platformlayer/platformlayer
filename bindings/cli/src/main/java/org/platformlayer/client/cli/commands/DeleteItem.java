package org.platformlayer.client.cli.commands;

import java.io.PrintWriter;

import org.codehaus.jettison.json.JSONException;
import org.kohsuke.args4j.Argument;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.client.cli.model.ItemPath;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.jobs.model.JobData;

public class DeleteItem extends PlatformLayerCommandRunnerBase {
	@Argument(index = 0)
	public ItemPath path;

	public DeleteItem() {
		super("delete", "item");
	}

	@Override
	public Object runCommand() throws PlatformLayerClientException, JSONException {
		PlatformLayerClient client = getPlatformLayerClient();

		PlatformLayerKey key = path.resolve(getContext());

		JobData jobData = client.deleteItem(key);
		return jobData;
	}

	@Override
	public void formatRaw(Object o, PrintWriter writer) {
		JobData jobData = (JobData) o;
		writer.println(jobData.key);
	}
}
