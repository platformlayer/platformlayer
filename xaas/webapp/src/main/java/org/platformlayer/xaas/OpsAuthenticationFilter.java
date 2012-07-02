package org.platformlayer.xaas;

import javax.crypto.SecretKey;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.openstack.keystone.service.AuthenticationTokenValidator;
import org.openstack.keystone.service.KeystoneAuthentication;
import org.openstack.keystone.service.OpenstackAuthenticationFilterBase;
import org.platformlayer.RepositoryException;
import org.platformlayer.Scope;
import org.platformlayer.auth.OpsProject;
import org.platformlayer.auth.OpsUser;
import org.platformlayer.auth.UserRepository;
import org.platformlayer.crypto.AesUtils;
import org.platformlayer.crypto.CryptoUtils;
import org.platformlayer.model.Authentication;
import org.platformlayer.ops.auth.OpsAuthentication;
import org.platformlayer.xaas.keystone.KeystoneUser;

public class OpsAuthenticationFilter extends OpenstackAuthenticationFilterBase {
	static final Logger log = Logger.getLogger(OpsAuthenticationFilter.class);
	private static final long MAX_TIMESTAMP_SKEW = 300L * 1000L;

	@Inject
	public OpsAuthenticationFilter(AuthenticationTokenValidator authenticationTokenValidator) {
		super(authenticationTokenValidator);
	}

	@Inject
	UserRepository userRepository;

	@Override
	protected void populateScope(Scope authenticatedScope, Authentication auth) throws Exception {
		super.populateScope(authenticatedScope, auth);

		OpsProject project;
		OpsUser user = null;
		if (auth instanceof DirectAuthentication) {
			project = ((DirectAuthentication) auth).getOpsProject();
			if (project == null) {
				throw new IllegalStateException();
			}
		} else {
			KeystoneUser keystoneUser = new KeystoneUser((KeystoneAuthentication) auth);
			user = keystoneUser;

			String projectKey = auth.getProject().getName();
			project = userRepository.findProject(user, projectKey);

			if (project == null) {
				log.warn("Project not found: " + projectKey);
				throw new SecurityException();
			}
		}

		OpsAuthentication opsAuthentication = new OpsAuthentication(auth, user, project);

		authenticatedScope.put(OpsAuthentication.class, opsAuthentication);
	}

	@Override
	protected Authentication attemptAuthentication(HttpServletRequest httpRequest) throws Exception {
		Authentication authenticated = super.attemptAuthentication(httpRequest);

		if (authenticated == null) {
			// TODO: Enforce SSL
			String authKey = httpRequest.getHeader("X-Auth-Key");
			String authSecret = httpRequest.getHeader("X-Auth-Secret");

			if (authKey != null && authSecret != null) {
				authenticated = attemptDirectAuthentication(httpRequest, authKey, authSecret);
			}
		}

		return authenticated;
	}

	private Authentication attemptDirectAuthentication(HttpServletRequest httpRequest, String authKey,
			String secretString) throws RepositoryException {
		// TODO: Require SSL??

		// long t = Long.parseLong(timestampString);
		// long delta = Math.abs(t - System.currentTimeMillis());
		// if (delta > MAX_TIMESTAMP_SKEW) {
		// // If the times are out of sync, that isn't a secret
		// throw new SecurityException("Timestamp skew too large");
		// }

		String projectPrefix = "project:";

		if (authKey.startsWith(projectPrefix)) {
			String projectKey = authKey.substring(projectPrefix.length());

			SecretKey secret;
			try {
				secret = AesUtils.deserializeKey(CryptoUtils.fromBase64(secretString));
			} catch (Exception e) {
				log.debug("Error while deserializing user provided secret", e);
				return null;
			}

			OpsProject project = userRepository.authenticateProject(projectKey, secret);
			if (project == null) {
				return null;
			}

			return new DirectAuthentication(project);
		} else {
			return null;
		}
	}
}
