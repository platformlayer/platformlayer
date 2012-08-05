package org.platformlayer.gwt.client.api.platformlayer;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.ImplementedBy;

@ImplementedBy(CorsPlatformLayerService.class)
public interface PlatformLayerService {
	void listRoots(OpsProject project, AsyncCallback<UntypedItemCollection> callback);

	void listJobs(OpsProject project, AsyncCallback<JobCollection> callback);

	void doAction(OpsProject project, String targetItem, Action action, AsyncCallback<Job> callback);

	void getJob(OpsProject project, String jobId, String tree, int skipLogLines, AsyncCallback<Job> callback);

	void getItem(OpsProject project, String key, AsyncCallback<UntypedItem> callback);
}
