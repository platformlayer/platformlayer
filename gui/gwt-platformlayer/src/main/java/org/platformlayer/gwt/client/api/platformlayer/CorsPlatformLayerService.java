package org.platformlayer.gwt.client.api.platformlayer;

import java.util.logging.Logger;

import javax.inject.Singleton;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.rpc.AsyncCallback;

@Singleton
public class CorsPlatformLayerService implements PlatformLayerService {
	static final Logger log = Logger.getLogger(CorsPlatformLayerService.class.getName());

	@Override
	public void listRoots(final OpsProject project, final AsyncCallback<UntypedItemCollection> callback) {
		String url = project.getProjectBaseUrl() + "roots";
		AuthenticatedCorsRequest.get(project, url).execute(callback);
	}

	@Override
	public void listJobs(OpsProject project, AsyncCallback<JobCollection> callback) {
		String url = project.getProjectBaseUrl() + "jobs";
		AuthenticatedCorsRequest.get(project, url).execute(callback);
	}

	@Override
	public void doAction(OpsProject project, String targetItem, Action action, AsyncCallback<Job> callback) {
		String url = toItemUrl(project, targetItem);
		url += "/actions";

		String json = new JSONObject(action).toString();
		AuthenticatedCorsRequest.post(project, url, json).execute(callback);
	}

	@Override
	public void getJob(OpsProject project, String jobId, String tree, int skipLogLines, AsyncCallback<Job> callback) {
		String url = project.getProjectBaseUrl() + "jobs/" + jobId;

		AuthenticatedCorsRequest request = AuthenticatedCorsRequest.get(project, url);

		if (tree != null) {
			request.add("tree", tree);
		}

		if (skipLogLines != 0) {
			request.add("log.skip", skipLogLines);
		}

		request.execute(callback);
	}

	@Override
	public void getItem(OpsProject project, String key, AsyncCallback<UntypedItem> callback) {
		String url = toItemUrl(project, key);
		AuthenticatedCorsRequest.get(project, url).execute(callback);
	}

	private String toItemUrl(OpsProject project, String item) {
		if (item.startsWith("platform://")) {
			item = item.substring("platform://".length());
		} else {
			throw new IllegalArgumentException();
		}

		String[] tokens = item.split("/");
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
		return url;
	}
}
