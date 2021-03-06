package org.platformlayer.auth.services;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.platformlayer.RepositoryException;
import org.platformlayer.auth.ProjectEntity;
import org.platformlayer.auth.UserEntity;
import org.platformlayer.auth.keystone.AuthenticationSecrets;
import org.platformlayer.auth.keystone.KeystoneUserAuthenticator;
import org.platformlayer.auth.model.Access;
import org.platformlayer.auth.model.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.TimeSpan;
import com.google.common.collect.Lists;

@Singleton
public class TokenHelpers {
	private static final Logger log = LoggerFactory.getLogger(TokenHelpers.class);

	protected static final TimeSpan TOKEN_VALIDITY = new TimeSpan("1h");

	@Inject
	TokenService tokenService;

	@Inject
	AuthenticationSecrets authSecrets;

	@Inject
	KeystoneUserAuthenticator userAuthenticator;

	public Access buildAccess(UserEntity user) {
		byte[] tokenSecret = authSecrets.buildToken(user.getUserSecret());

		TokenInfo token = buildToken("" + user.getId(), tokenSecret);

		Access access = new Access();
		// response.access.serviceCatalog = serviceMapper.getServices(userInfo,
		// project);
		access.token = new Token();
		access.token.expires = token.expiration;
		access.token.id = tokenService.encodeToken(token);

		access.projects = Lists.newArrayList();
		try {
			for (ProjectEntity project : userAuthenticator.listProjects(user)) {
				access.projects.add(project.getName());
			}
		} catch (RepositoryException e) {
			log.warn("Error while listing projects for user: " + user.key, e);
			throw new IllegalStateException("Error listing projects for user", e);
		}

		return access;
	}

	private TokenInfo buildToken(String userId, byte[] tokenSecret) {
		Date now = new Date();
		Date expiration = TOKEN_VALIDITY.addTo(now);

		byte flags = 0;
		TokenInfo tokenInfo = new TokenInfo(flags, userId, expiration, tokenSecret);

		return tokenInfo;
	}
}
