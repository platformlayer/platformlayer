package org.openstack.keystone.services;

import java.security.Principal;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.openstack.keystone.model.Role;
import org.openstack.utils.Hex;
import org.platformlayer.ApplicationMode;
import org.platformlayer.TimeSpan;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class AuthenticationFacade {
	static final Logger log = Logger.getLogger(AuthenticationFacade.class);

	private static final TimeSpan CACHE_USERINFO = TimeSpan.FIVE_MINUTES;

	@Inject
	UserAuthenticator userAuthenticator;

	@Inject
	SystemAuthenticator systemAuthenticator;

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

	public AuthenticationInfo authenticate(boolean isSystem, String username, String password)
			throws AuthenticatorException {
		AuthenticationInfo userId;
		if (isSystem) {
			userId = systemAuthenticator.authenticate(username, password);
		} else {
			userId = userAuthenticator.authenticate(username, password);
		}
		if (userId == null) {
			return null;
		}
		return userId;
	}

	public UserInfo getUserInfo(boolean isSystem, String userId, byte[] tokenSecret) throws AuthenticatorException {
		String key = (isSystem ? "sys:" : "user:") + userId;
		UserInfo userInfo = cache.lookup(key, UserInfo.class);
		if (userInfo == null) {
			// Find groups
			GenericAuthenticator authenticator = isSystem ? systemAuthenticator : userAuthenticator;
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

	public TokenInfo validateToken(boolean system, String token) {
		if (ApplicationMode.isDevelopment()) {
			if (system && Objects.equal(token, "auth_token")) {
				byte flags = TokenInfo.FLAG_SYSTEM;
				String scope = null;
				String userId = "system";
				Date expiration = null;
				byte[] tokenSecret = null;
				TokenInfo tokenInfo = new TokenInfo(flags, scope, userId, expiration, tokenSecret);
				return tokenInfo;
			}
		}
		TokenInfo tokenInfo = tokenService.decodeToken(system, token);
		if (tokenInfo == null || tokenInfo.hasExpired()) {
			return null;
		}
		return tokenInfo;
	}

	public String signToken(TokenInfo token) {
		return tokenService.encodeToken(token);
	}

	public AuthenticationInfo authenticate(boolean isSystem, X509Certificate[] certChain) {
		X509Certificate head = certChain[0];

		Principal subject = head.getSubjectDN();
		PublicKey publicKey = head.getPublicKey();

		String publicKeyHex = Hex.toHex(publicKey.getEncoded());
		Set<String> trusted = Sets.newHashSet();
		trusted.add("30820122300d06092a864886f70d01010105000382010f003082010a0282010100945d7ba2d1513eeff00eef508025e1dde5e5b6fc2bbfdd54c75e8367b930bf2e137e01e93ab619a1bc6d6bd736ae3ac596711eeea34eabd7fce7c2114727c012f3e1ff31cea64176ef06210c4a35fed4195573010dec50918839077d77968c19147d38d1f865747b107576cada9dbe08a0e9188a197e2708ed6be55e8a8ba0ebbbd9c2ca4bc1c9ba083baddcc61bce8872e56722596523bbf6e994dbaca08c7e582656ca873d85ee076bd57df0d8255f519b2bd14632af9778500d41ac29568a2701d04bf44ae731c9699fd248b533fa28c88c7deb8bd55e44cd680fa8618873e3cc4e9cde8c6db51c45ca93938ed5d76173388497059521930f9e01cd70872d0203010001");

		if (trusted.contains(publicKeyHex)) {
			return new AuthenticationInfo(subject.getName(), null);
		}

		log.debug("Authentication failed - public key not recognized: " + publicKeyHex);

		return null;
	}

	// public UserInfo getUserInfoByUsername(boolean isSystem, String username) throws AuthenticatorException {
	// // We assume userId == username (for now)
	// String userId = username;
	// return getUserInfo(isSystem, userId);
	// }

}
