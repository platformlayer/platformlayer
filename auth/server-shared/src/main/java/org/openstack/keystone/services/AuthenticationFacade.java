package org.openstack.keystone.services;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.openstack.keystone.model.Role;
import org.platformlayer.TimeSpan;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class AuthenticationFacade {
	static final Logger log = Logger.getLogger(AuthenticationFacade.class);

	private static final TimeSpan CACHE_USERINFO = TimeSpan.FIVE_MINUTES;

	@Inject
	UserAuthenticator userAuthenticator;

	@Inject
	TokenService tokenService;

	@Inject
	GroupToTenantMapper groupToTenantMapper;

	@Inject
	CacheSystem cache;

	public List<Role> getRoles(UserInfo userInfo, String filterTenantId) {
		Collection<String> groups = userInfo.groups;
		List<Role> roles = Lists.newArrayList();
		for (String group : groups) {
			TenantInfo tenantInfo = groupToTenantMapper.mapGroupToTenant(group);
			if (tenantInfo != null) {
				if (filterTenantId != null && !Objects.equal(filterTenantId, tenantInfo.tenantId)) {
					continue;
				}

				Role role = new Role();
				role.description = tenantInfo.roleId;
				role.name = tenantInfo.roleId;
				role.tenantId = tenantInfo.tenantId;

				roles.add(role);
			}
		}

		return roles;
	}

	public AuthenticationInfo authenticate(String username, String password) throws AuthenticatorException {
		GenericAuthenticator authenticator = userAuthenticator;

		AuthenticationInfo auth = authenticator.authenticate(username, password);
		return auth;
	}

	public UserInfo getUserInfo(String userId, byte[] tokenSecret) throws AuthenticatorException {
		String key = "user:" + userId;
		UserInfo userInfo = cache.lookup(key, UserInfo.class);
		if (userInfo == null) {
			// Find groups
			GenericAuthenticator authenticator = userAuthenticator;
			GroupResolver groupResolver = new GroupResolver(authenticator.getGroupMembership());
			Collection<String> groups = groupResolver.findGroups(userId);

			// We assume username == userId (for now)
			String username = userId;

			String email = null;

			byte[] userSecret = null;
			if (tokenSecret != null) {
				userSecret = authenticator.getUserSecret(userId, tokenSecret);
			}

			userInfo = new UserInfo(userId, username, email, userSecret, groups);
			cache.put(key, CACHE_USERINFO, userInfo);
		}
		return userInfo;
	}

	public TokenInfo validateToken(String token) {
		TokenInfo tokenInfo = tokenService.decodeToken(token);
		if (tokenInfo == null || tokenInfo.hasExpired()) {
			return null;
		}
		return tokenInfo;
	}

	public String signToken(TokenInfo token) {
		return tokenService.encodeToken(token);
	}

	// public UserInfo getUserInfoByUsername(boolean isSystem, String username) throws AuthenticatorException {
	// // We assume userId == username (for now)
	// String userId = username;
	// return getUserInfo(isSystem, userId);
	// }

}
