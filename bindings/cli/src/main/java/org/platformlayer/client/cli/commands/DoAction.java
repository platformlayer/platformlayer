package org.platformlayer.client.cli.commands;

import java.io.PrintWriter;

import org.json.JSONException;
import org.json.JSONObject;
import org.kohsuke.args4j.Argument;
import org.platformlayer.Format;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.client.cli.model.CliAction;
import org.platformlayer.client.cli.model.ItemPath;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.jobs.model.JobData;

public class DoAction extends PlatformLayerCommandRunnerBase {
	@Argument(index = 0, usage = "path", required = true)
	public ItemPath path;

	@Argument(index = 1, usage = "action", required = true)
	public CliAction action;

	@Argument(index = 2, usage = "data", required = false)
	public String json;

	public DoAction() {
		super("do", "action");
	}

	@Override
	public Object runCommand() throws PlatformLayerClientException {
		String json;

		try {
			JSONObject data;
			if (this.json != null) {
				data = new JSONObject(this.json);
			} else {
				data = new JSONObject();
			}
			data.put("type", action.getKey());

			json = data.toString();
		} catch (JSONException e) {
			throw new IllegalStateException("Error building JSON", e);
		}

		PlatformLayerClient client = getPlatformLayerClient();

		PlatformLayerKey key = path.resolve(getContext());

		JobData ret = client.doAction(key, json, Format.JSON);
		return ret;
	}

	@Override
	public void formatRaw(Object o, PrintWriter writer) {
		JobData jobData = (JobData) o;
		writer.println(jobData.getJobId());
	}

}
