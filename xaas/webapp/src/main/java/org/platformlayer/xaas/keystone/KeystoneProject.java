package org.platformlayer.xaas.keystone;

import javax.crypto.SecretKey;

import org.apache.log4j.Logger;
import org.platformlayer.auth.OpsProject;
import org.platformlayer.model.ProjectAuthentication;

public class KeystoneProject implements OpsProject {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(KeystoneProject.class);

	private final KeystoneUser keystoneUser;

	private final ProjectAuthentication project;

	public KeystoneProject(KeystoneUser keystoneUser, ProjectAuthentication project) {
		this.keystoneUser = keystoneUser;
		this.project = project;
	}

	@Override
	public String getName() {
		return project.getName();
	}

	@Override
	public boolean isLocked() {
		return false;
	}

	@Override
	public SecretKey getProjectSecret() {
		return project.getSecret();
	}

	@Override
	public int getId() {
		return Integer.parseInt(project.getId());
	}
}
