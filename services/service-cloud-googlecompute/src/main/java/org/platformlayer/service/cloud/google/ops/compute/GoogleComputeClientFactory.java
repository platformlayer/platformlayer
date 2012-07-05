package org.platformlayer.service.cloud.google.ops.compute;

import java.security.PrivateKey;

import org.platformlayer.crypto.CryptoUtils;
import org.platformlayer.crypto.RsaUtils;
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

	public static GoogleComputeClient getComputeClient(GoogleCloud cloud) {
		PrivateKey privateKey = RsaUtils.deserializePrivateKey(CryptoUtils.fromBase64(cloud.serviceAccountKey
				.plaintext()));

		// Build service account credential.
		GoogleCredential credential = new GoogleCredential.Builder().setTransport(HTTP_TRANSPORT)
				.setJsonFactory(JSON_FACTORY).setServiceAccountId(cloud.serviceAccountId)
				.setServiceAccountScopes(ComputeScopes.COMPUTE).setServiceAccountPrivateKey(privateKey).build();

		Compute compute = new Compute(HTTP_TRANSPORT, JSON_FACTORY, credential);

		return new GoogleComputeClient(compute, cloud.projectId);
	}
}
