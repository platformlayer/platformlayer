package org.platformlayer.gwt.client.api.platformlayer;

import java.util.logging.Logger;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class CorsPlatformLayerService implements PlatformLayerService {
	static final Logger log = Logger.getLogger(CorsPlatformLayerService.class.getName());

	@Override
	public void listRoots(final OpsProject project, final AsyncCallback<UntypedItemCollection> callback) {
		String url = project.getProjectBaseUrl() + "roots";
		CorsRequest<UntypedItemCollection> request = new CorsRequest<UntypedItemCollection>(project, url);
		request.execute(callback);
	}

	@Override
	public void listJobs(OpsProject project, AsyncCallback<JobCollection> callback) {
		String url = project.getProjectBaseUrl() + "jobs";
		CorsRequest<JobCollection> request = new CorsRequest<JobCollection>(project, url);
		request.execute(callback);
	}

	@Override
	public void doAction(OpsProject project, String targetItem, Action action, AsyncCallback<Job> callback) {
		String url = project.getProjectBaseUrl() + targetItem + "/actions";
		String json = new JSONObject(action).toString();
		CorsRequest<Job> request = new CorsRequest<Job>(project, url, json);
		request.execute(callback);
	}

	@Override
	public void getJobLog(OpsProject project, String jobId, AsyncCallback<JobLog> callback) {
		String url = project.getProjectBaseUrl() + "jobs/" + jobId + "/log";
		CorsRequest<JobLog> request = new CorsRequest<JobLog>(project, url, null);
		request.execute(callback);
	}

	@Override
	public void getItem(OpsProject project, String key, AsyncCallback<UntypedItem> callback) {
		if (key.startsWith("platform://")) {
			key = key.substring("platform://".length());
		} else {
			throw new IllegalArgumentException();
		}

		String[] tokens = key.split("/");
		if (tokens.length != 5) {
			throw new IllegalArgumentException();
		}

		String host = tokens[0];
		if (!host.isEmpty()) {
			throw new IllegalArgumentException();
		}

		String projectKey = tokens[1];
		if (!project.getProjectName().equals(projectKey)) {
			throw new IllegalArgumentException();
		}

		String service = tokens[2];
		String itemType = tokens[3];
		String itemKey = tokens[4];

		String url = project.getProjectBaseUrl() + service + "/" + itemType + "/" + itemKey;
		CorsRequest<UntypedItem> request = new CorsRequest<UntypedItem>(project, url, null);
		request.execute(callback);
	}
}
