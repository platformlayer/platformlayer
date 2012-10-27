package org.platformlayer.service.cloud.openstack.ops;

import java.io.File;

import org.openstack.client.OpenstackCredentials;
import org.openstack.client.common.RequestBuilder;
import org.openstack.client.storage.OpenstackStorageClient;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.helpers.CurlRequest;
import org.platformlayer.ops.process.ProcessExecution;

public class DirectOpenstackDownload {
	public void download(OpsTarget target, File targetPath, OpenstackCredentials credentials, String containerName,
			String objectPath) throws OpsException {

		RemoteCurlOpenstackSession session = new RemoteCurlOpenstackSession(target);
		session.authenticate(credentials, false);

		OpenstackStorageClient storageClient = session.getStorageClient();

		RequestBuilder request = storageClient.root().containers().id(containerName).objects().id(objectPath)
				.buildDownloadRequest();

		CurlRequest curlRequest = session.toCurlRequest(request);
		curlRequest.bareRequest = true;

		Command curlCommand = curlRequest.toCommand();
		curlCommand.addLiteral(">");
		curlCommand.addFile(targetPath);

		ProcessExecution execution = target.executeCommand(curlCommand);

		// CurlResult curlResult = curlRequest.parseResponse(execution);
		//
		// int httpResult = curlResult.getHttpResult();
		// switch (httpResult) {
		// case 200:
		// break;
		// case 201:
		// break;
		// default:
		// throw new OpsException("Unexpected result code while downloading file: " + httpResult + " Result=" +
		// curlResult);
		// }
	}
}