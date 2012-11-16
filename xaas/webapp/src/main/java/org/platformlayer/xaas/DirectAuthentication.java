package org.platformlayer.xaas;

import java.util.List;

import org.apache.log4j.Logger;
import org.platformlayer.auth.AuthenticationToken;
import org.platformlayer.auth.DirectAuthenticationToken;
import org.platformlayer.crypto.CryptoUtils;
import org.platformlayer.model.Authentication;
import org.platformlayer.model.AuthenticationCredentials;
import org.platformlayer.model.ProjectAuthorization;
import org.platformlayer.model.RoleId;

import com.fathomdb.crypto.CryptoKey;
import com.fathomdb.crypto.FathomdbCrypto;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class DirectAuthentication implements AuthenticationCredentials, ProjectAuthorization {
	static final Logger log = Logger.getLogger(DirectAuthentication.class);

	private final ProjectAuthorization project;

	public DirectAuthentication(ProjectAuthorization project) {
		this.project = project;
	}

	// @Override
	// public boolean isInRole(String project, RoleId role) {
	// log.warn("Assuming direct authentication is in all roles");
	// return true;
	// }

	public ProjectAuthorization getOpsProject() {
		return project;
	}

	@Override
	public String toString() {
		return "DirectAuthentication [project=" + project + "]";
	}

	@Override
	public String getName() {
		return project.getName();
	}

	@Override
	public int getId() {
		return project.getId();
	}

	@Override
	public CryptoKey getProjectSecret() {
		return project.getProjectSecret();
	}

	@Override
	public Authentication getUser() {
		throw new UnsupportedOperationException();
	}

	@Override
	public AuthenticationToken getToken() {
		throw new UnsupportedOperationException();
	}

	public static DirectAuthentication build(String authKey, String authSecret) {
		// TODO: Require SSL??

		// long t = Long.parseLong(timestampString);
		// long delta = Math.abs(t - System.currentTimeMillis());
		// if (delta > MAX_TIMESTAMP_SKEW) {
		// // If the times are out of sync, that isn't a secret
		// throw new SecurityException("Timestamp skew too large");
		// }

		ProjectAuthorization project = null;

		String projectPrefix = DirectAuthenticationToken.PREFIX;

		if (authKey.startsWith(projectPrefix)) {
			List<String> projectTokens = Lists.newArrayList(Splitter.on(':').limit(3).split(authKey));
			if (projectTokens.size() == 3) {
				final String projectKey = projectTokens.get(2);
				final int projectId = Integer.parseInt(projectTokens.get(1));

				final CryptoKey secret;
				try {
					secret = FathomdbCrypto.deserializeKey(CryptoUtils.fromBase64(authSecret));
				} catch (Exception e) {
					log.debug("Error while deserializing user provided secret", e);
					return null;
				}

				project = new ProjectAuthorization() {
					@Override
					public boolean isLocked() {
						return secret == null;
					}

					@Override
					public CryptoKey getProjectSecret() {
						return secret;
					}

					@Override
					public int getId() {
						return projectId;
					}

					@Override
					public String getName() {
						return projectKey;
					}

					@Override
					public Authentication getUser() {
						throw new UnsupportedOperationException();
					}

					@Override
					public List<RoleId> getRoles() {
						throw new UnsupportedOperationException();
					}
				};
			}
		}

		if (project == null) {
			return null;
		}

		return new DirectAuthentication(project);

	}

	@Override
	public boolean isLocked() {
		return project.isLocked();
	}

	@Override
	public List<RoleId> getRoles() {
		log.info("Assuming OWNER role for DirectAuthentication");

		List<RoleId> roles = Lists.newArrayList();
		roles.add(RoleId.OWNER);
		return roles;
	}
}
