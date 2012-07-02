package org.platformlayer.xaas;

import javax.crypto.SecretKey;

import org.apache.log4j.Logger;
import org.platformlayer.auth.OpsProject;
import org.platformlayer.model.Authentication;
import org.platformlayer.model.ProjectAuthentication;
import org.platformlayer.model.RoleId;

public class DirectAuthentication implements Authentication {
	static final Logger log = Logger.getLogger(DirectAuthentication.class);

	private final OpsProject project;

	public DirectAuthentication(OpsProject project) {
		this.project = project;
	}

	@Override
	public ProjectAuthentication getProject() {
		return new ProjectAuthentication() {
			@Override
			public SecretKey getSecret() {
				return project.getProjectSecret();
			}

			@Override
			public String getName() {
				return project.getName();
			}

			@Override
			public String getId() {
				return "" + project.getId();
			}
		};
	}

	@Override
	public String getUserKey() {
		return null;
	}

	@Override
	public boolean isInRole(String project, RoleId role) {
		log.warn("Assuming direct authentication is in all roles");
		return true;
	}

	@Override
	public byte[] getUserSecret() {
		return null;
	}

	public OpsProject getOpsProject() {
		return project;
	}

	@Override
	public String toString() {
		return "DirectAuthentication [project=" + project + "]";
	}
}
