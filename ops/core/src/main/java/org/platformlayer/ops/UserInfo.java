package org.platformlayer.ops;

import javax.crypto.SecretKey;

import org.platformlayer.DirectPlatformLayerClient;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.TypedItemMapper;
import org.platformlayer.TypedPlatformLayerClient;
import org.platformlayer.auth.AuthenticationToken;
import org.platformlayer.auth.DirectAuthenticationToken;
import org.platformlayer.auth.DirectAuthenticator;
import org.platformlayer.auth.OpsProject;
import org.platformlayer.federation.FederatedPlatformLayerClient;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.ops.auth.OpsAuthentication;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.machines.PlatformLayerTypedItemMapper;

public class UserInfo {
	// final OpenstackComputeConfiguration openstackComputeConfiguration;
	// final String glanceBaseUri;

	final OpsConfig config;
	final OpsAuthentication auth;

	public UserInfo(OpsAuthentication auth, OpsConfig config) {
		this.auth = auth;
		this.config = config;
		// super();
		// OpenstackComputeConfiguration openstackComputeConfiguration, String
		// glanceBaseUri
		// this.openstackComputeConfiguration = openstackComputeConfiguration;
		// this.glanceBaseUri = glanceBaseUri;
	}

	public ProjectId getProjectId() {
		return auth.getProjectId();
	}

	// private OpenstackComputeClient openstackComputeClient;

	// static class Ec2ComputeConfiguration {
	// String endpoint;
	// AWSCredentials awsCredentials;
	// }

	// public OpenstackComputeClient getOpenstackComputeClient() throws OpsException {
	// if (openstackComputeClient == null) {
	// try {
	// openstackComputeClient = OpenstackComputeClient.loginUsingConfiguration(getOpenstackComputeConfiguration());
	// } catch (OpenstackException e) {
	// throw new OpsException("Error connecting to OpenStack compute API", e);
	// }
	// // ComputeConfiguration computeConfig =
	// // getOpenstackComputeConfiguration();
	// // AmazonEC2Client client = new
	// // AmazonEC2Client(computeConfig.awsCredentials);
	// // client.setEndpoint(computeConfig.endpoint);
	// //
	// // openstackComputeClient = client;
	// }
	// return openstackComputeClient;
	// }

	// public AmazonEC2Client buildEc2Client() throws OpsException {
	// Ec2ComputeConfiguration computeConfig = getEc2ComputeConfiguration();
	// AmazonEC2Client client = new AmazonEC2Client(computeConfig.awsCredentials);
	// client.setEndpoint(computeConfig.endpoint);
	//
	// return client;
	// }

	// private Ec2ComputeConfiguration getEc2ComputeConfiguration() throws OpsConfigException {
	// Ec2ComputeConfiguration computeConfig = new Ec2ComputeConfiguration();
	// computeConfig.endpoint = config.getRequiredString("compute.ec2.url");
	//
	// String username = config.getRequiredString("compute.ec2.username");
	// String secret = config.getRequiredString("compute.ec2.secret");
	//
	// computeConfig.awsCredentials = new BasicAWSCredentials(username, secret);
	// return computeConfig;
	// }

	// private OpenstackComputeConfiguration getOpenstackComputeConfiguration() throws OpsConfigException {
	// // ComputeConfiguration computeConfig = new ComputeConfiguration();
	// // computeConfig.endpoint = config.getRequiredString("compute.url");
	// //
	// // String username = config.getRequiredString("compute.username");
	// // String secret = config.getRequiredString("compute.secret");
	// //
	// // computeConfig.awsCredentials = new BasicAWSCredentials(username, secret);
	// // return computeConfig;
	//
	// String url = config.getRequiredString("compute.os.auth");
	//
	// String username = config.getRequiredString("compute.os.username");
	// String secret = config.getRequiredString("compute.os.secret");
	// return new OpenstackComputeConfiguration(url, username, secret);
	// }

	private PlatformLayerClient platformLayerClient;

	public PlatformLayerClient getPlatformLayerClient() throws OpsException {
		if (platformLayerClient == null) {
			// // String conductorUri = config.getRequiredString("conductor.url");
			// String tenant = config.getRequiredString("conductor.tenant");
			// String username = config.getRequiredString("conductor.username");
			// String secret = config.getRequiredString("conductor.secret");
			// String authUrl = config.getRequiredString("conductor.auth");
			//
			// Authenticator authenticator = new KeystoneAuthenticator(tenant, username, secret, authUrl);
			// platformLayerClient = PlatformLayerClient.build(authenticator, new ProjectId(tenant));

			OpsProject project = getProject();
			String keyId = "project:" + project.key;
			SecretKey secret = project.getProjectSecret();
			String platformLayerUrl = OpsSystem.getPlatformLayerUrlBase();
			platformLayerUrl += project.key;
			AuthenticationToken token = new DirectAuthenticationToken(platformLayerUrl, keyId, secret);
			DirectAuthenticator directAuthenticator = new DirectAuthenticator(token);

			DirectPlatformLayerClient localClient = DirectPlatformLayerClient
					.build(directAuthenticator, getProjectId());
			TypedPlatformLayerClient typedLocalClient = PlatformLayerHelpers.build(localClient);

			TypedItemMapper mapper = Injection.getInstance(PlatformLayerTypedItemMapper.class);

			platformLayerClient = FederatedPlatformLayerClient.build(typedLocalClient, mapper);
		}
		return platformLayerClient;
	}

	public OpsConfig getConfig() {
		return config;
	}

	// public int getUserId() {
	// return auth.getUserId();
	// }

	public SecretKey findUserSecret() {
		return auth.findUserSecret();
	}

	public String getUserKey() {
		return auth.getUserKey();
	}

	public OpsProject getProject() {
		return auth.getProject();
	}
}
