package org.platformlayer.service.cloud.openstack.backups;

import java.io.File;
import java.util.Map;
import java.util.UUID;

import org.openstack.client.common.RequestBuilder;
import org.openstack.client.storage.OpenstackStorageClient;
import org.openstack.model.storage.ObjectProperties;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.FileUpload;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.backups.Backup;
import org.platformlayer.ops.backups.DirectoryBackup;
import org.platformlayer.ops.helpers.CurlRequest;
import org.platformlayer.ops.helpers.CurlResult;
import org.platformlayer.ops.process.ProcessExecution;
import org.platformlayer.service.cloud.openstack.ops.RemoteCurlOpenstackSession;
import org.platformlayer.service.cloud.openstack.ops.RemoteCurlOpenstackSession.RemoteCurlOpenstackRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

public class ShellBackupClient {

	private static final Logger log = LoggerFactory.getLogger(ShellBackupClient.class);

	public final OpenstackBackupContext context;

	ShellBackupClient(OpenstackBackupContext context) {
		super();
		this.context = context;
	}

	// private RemoteCurlOpenstackSession openstackSession = null;
	//
	// private RemoteCurlOpenstackSession getOpenstackSession(OpsTarget target) {
	// if (openstackSession == null) {
	// RemoteCurlOpenstackSession session = new RemoteCurlOpenstackSession(target);
	//
	// session.authenticate(credentials, false);
	//
	// openstackSession = session;
	// }
	// return openstackSession;
	// }

	private OpenstackStorageClient storageClient = null;

	private OpenstackStorageClient getStorageClient(OpsTarget target) {
		if (storageClient == null) {
			RemoteCurlOpenstackSession session = RemoteCurlOpenstackSession
					.build(target, context.getOpenstackSession());

			storageClient = session.getStorageClient();
		}
		return storageClient;
	}

	public void doBackup(DirectoryBackup request) throws OpsException {
		File tempDir = request.target.createTempDir();
		File excludeFile = new File(tempDir, "exclude.txt");

		if (request.objectName == null) {
			request.objectName = UUID.randomUUID().toString();
		}
		request.objectName += ".tgz";

		// TODO: Set content type?

		FileUpload.upload(request.target, excludeFile, Joiner.on("\n").join(request.exclude));

		Command tarCommand = Command.build("tar zcf - -X {0} {1}", excludeFile, request.rootDirectory);

		log.info("Backing up " + request.rootDirectory);

		uploadStream(request, tarCommand);

		request.target.rmdir(tempDir);

		log.info("Backup complete");
	}

	public void uploadStream(Backup request, Command dataSourceCommand) throws OpsException {
		ObjectProperties openstackProperties = new ObjectProperties();

		if (request.objectName == null) {
			throw new IllegalArgumentException("objectName is required");
		}

		String objectPath = context.toPath(request.objectName);
		openstackProperties.setName(objectPath);

		for (Map.Entry<String, String> entry : request.objectProperties.entrySet()) {
			String key = entry.getKey();
			openstackProperties.getCustomProperties().put(key, entry.getValue());
		}

		log.info("Uploading to " + getContainerName() + "/" + objectPath);

		RequestBuilder requestBuilder = getStorageClient(request.target).root().containers().id(getContainerName())
				.objects().buildPutRequest(openstackProperties);

		CurlRequest curlRequest = ((RemoteCurlOpenstackRequest) requestBuilder).toCurlRequest();
		curlRequest.bodyFromStdin = true;

		Command curlCommand = curlRequest.toCommand();
		Command pipedCommand = dataSourceCommand.pipeTo(curlCommand);

		ProcessExecution execution = request.target.executeCommand(pipedCommand);

		CurlResult curlResult = curlRequest.parseResponse(execution);

		int httpResult = curlResult.getHttpResult();
		switch (httpResult) {
		case 200:
			break;
		case 201:
			break;
		default:
			throw new OpsException("Unexpected result code while uploading backup: " + httpResult + " Result="
					+ curlResult);
		}

	}

	private String getContainerName() {
		return context.getContainerName();
	}
}
