package org.platformlayer.gwt.client.api.platformlayer;

import org.platformlayer.gwt.client.api.login.Authentication;

public class OpsProject {
	final String platformlayerUrl;
	final String project;
	final Authentication authentication;

	public OpsProject(String platformlayerUrl, String project, Authentication authentication) {
		super();
		this.platformlayerUrl = platformlayerUrl;
		this.project = project;
		this.authentication = authentication;

		assert platformlayerUrl.endsWith("/");
		assert project != null;
		assert authentication != null;
	}

	public Authentication getAuthentication() {
		return authentication;
	}

	public String getProjectBaseUrl() {
		String baseUrl = platformlayerUrl + project + "/";

		return baseUrl;
	}

	public String getProjectName() {
		return project;
	}

}
