//package org.openstack.keystone.services;
//
//import javax.inject.Inject;
//
//import org.apache.log4j.Logger;
//import org.platformlayer.RepositoryException;
//import org.platformlayer.TimeSpan;
//import org.platformlayer.auth.OpsUser;
//import org.platformlayer.auth.ProjectEntity;
//import org.platformlayer.auth.UserEntity;
//import org.platformlayer.auth.keystone.KeystoneUserAuthenticator;
//
//public class AuthenticationFacade {
//	static final Logger log = Logger.getLogger(AuthenticationFacade.class);
//
//	private static final TimeSpan CACHE_USERINFO = TimeSpan.FIVE_MINUTES;
//
//	@Inject
//	KeystoneUserAuthenticator userAuthenticator;
//
//	@Inject
//	TokenService tokenService;
//
//	@Inject
//	GroupToTenantMapper groupToTenantMapper;
//
//	// @Inject
//	// CacheSystem cache;
//
//	// public List<Role> getRoles(OpsUser userInfo, String filterTenantId) {
//	// Collection<String> groups = userInfo.groups;
//	// List<Role> roles = Lists.newArrayList();
//	// for (String group : groups) {
//	// TenantInfo tenantInfo = groupToTenantMapper.mapGroupToTenant(group);
//	// if (tenantInfo != null) {
//	// if (filterTenantId != null && !Objects.equal(filterTenantId, tenantInfo.tenantId)) {
//	// continue;
//	// }
//	//
//	// Role role = new Role();
//	// role.description = tenantInfo.roleId;
//	// role.name = tenantInfo.roleId;
//	// role.tenantId = tenantInfo.tenantId;
//	//
//	// roles.add(role);
//	// }
//	// }
//	//
//	// return roles;
//	// }
//
//	public UserEntity authenticate(String username, String password) throws AuthenticatorException {
//		UserEntity auth = userAuthenticator.authenticate(username, password);
//		return auth;
//	}
//
//	// public UserEntity getUserInfo(String userId, byte[] tokenSecret) throws AuthenticatorException {
//	// String key = "user:" + userId;
//	// UserEntity user;
//	//
//	// // UserInfo userInfo = cache.lookup(key, UserInfo.class);
//	// // if (userInfo == null) {
//	// // Find groups
//	// GroupResolver groupResolver = new GroupResolver(userAuthenticator.getGroupMembership());
//	// Collection<String> groups = groupResolver.findGroups(userId);
//	//
//	// // We assume username == userId (for now)
//	// String username = userId;
//	//
//	// String email = null;
//	//
//	// byte[] userSecret = null;
//	// if (tokenSecret != null) {
//	// userSecret = userAuthenticator.getUserSecret(userId, tokenSecret);
//	// }
//	//
//	// userInfo = new UserInfo(userId, username, email, userSecret, groups);
//	// // cache.put(key, CACHE_USERINFO, userInfo);
//	// // }
//	// return user;
//	// }
//
//	// public TokenInfo validateToken(String token) {
//	// TokenInfo tokenInfo = tokenService.decodeToken(token);
//	// if (tokenInfo == null || tokenInfo.hasExpired()) {
//	// return null;
//	// }
//	// return tokenInfo;
//	// }
//
//	public String signToken(TokenInfo token) {
//		return tokenService.encodeToken(token);
//	}
//
//	public ProjectEntity findProject(String projectKey, OpsUser user) throws RepositoryException {
//		ProjectEntity project = userAuthenticator.findProject(projectKey, user);
//		return project;
//	}
//
//	// public UserInfo getUserInfoByUsername(boolean isSystem, String username) throws AuthenticatorException {
//	// // We assume userId == username (for now)
//	// String userId = username;
//	// return getUserInfo(isSystem, userId);
//	// }
//
// }
