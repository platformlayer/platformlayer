package org.platformlayer;

import java.util.List;

import org.platformlayer.auth.Authenticator;
import org.platformlayer.ids.ProjectId;

public class PlatformLayerEndpointInfo {
	final Authenticator authenticator;
	final String platformlayerBaseUrl;
	final ProjectId projectId;
	final List<String> trustKeys;

	public PlatformLayerEndpointInfo(Authenticator authenticator, String platformlayerBaseUrl, ProjectId projectId,
			List<String> trustKeys) {
		super();
		this.authenticator = authenticator;
		this.platformlayerBaseUrl = platformlayerBaseUrl;
		this.projectId = projectId;
		this.trustKeys = trustKeys;
	}

	public Authenticator getAuthenticator() {
		return authenticator;
	}

	public String getPlatformlayerBaseUrl() {
		return platformlayerBaseUrl;
	}

	public ProjectId getProjectId() {
		return projectId;
	}

	public List<String> getTrustKeys() {
		return trustKeys;
	}

}
