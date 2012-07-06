package org.platformlayer.service.cloud.google.ops.compute;

import java.security.PrivateKey;

import javax.inject.Inject;

import org.platformlayer.crypto.CryptoUtils;
import org.platformlayer.crypto.KeyParser;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.service.cloud.google.model.GoogleCloud;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.compute.Compute;
import com.google.api.services.compute.ComputeScopes;

public class GoogleComputeClientFactory {
	static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	static final JsonFactory JSON_FACTORY = new JacksonFactory();

	@Inject
	PlatformLayerHelpers platformLayerClient;

	public GoogleComputeClient getComputeClient(GoogleCloud cloud) throws OpsException {
		byte[] data = CryptoUtils.fromBase64(cloud.serviceAccountKey.plaintext());

		KeyParser parser = new KeyParser(data);

		Object parsed = parser.parse();
		PrivateKey privateKey;
		if (parsed instanceof PrivateKey) {
			privateKey = (PrivateKey) parsed;
		} else {
			throw new OpsException("Expected private key, found: " + parsed.getClass().getSimpleName());
		}

		// Build service account credential.
		GoogleCredential credential = new GoogleCredential.Builder().setTransport(HTTP_TRANSPORT)
				.setJsonFactory(JSON_FACTORY).setServiceAccountId(cloud.serviceAccountId)
				.setServiceAccountScopes(ComputeScopes.COMPUTE).setServiceAccountPrivateKey(privateKey).build();

		Compute compute = new Compute(HTTP_TRANSPORT, JSON_FACTORY, credential);

		return new GoogleComputeClient(platformLayerClient, compute, cloud.projectId);
	}
}
