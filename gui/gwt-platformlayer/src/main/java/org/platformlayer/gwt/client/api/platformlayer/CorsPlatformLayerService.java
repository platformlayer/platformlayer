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
}
