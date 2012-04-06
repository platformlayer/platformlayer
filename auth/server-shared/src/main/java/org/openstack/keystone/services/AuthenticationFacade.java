package org.openstack.keystone.services;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.openstack.keystone.model.Role;
import org.platformlayer.ApplicationMode;
import org.platformlayer.TimeSpan;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

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
                if (filterTenantId != null && !Objects.equal(filterTenantId, tenantInfo.tenantId))
                    continue;

                Role role = new Role();
                role.description = tenantInfo.roleId;
                role.name = tenantInfo.roleId;
                role.tenantId = tenantInfo.tenantId;

                roles.add(role);
            }
        }

        return roles;
    }

    public AuthenticationInfo authenticate(boolean isSystem, String username, String password) throws AuthenticatorException {
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
            if (system && token.equals("auth_token")) {
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

    // public UserInfo getUserInfoByUsername(boolean isSystem, String username) throws AuthenticatorException {
    // // We assume userId == username (for now)
    // String userId = username;
    // return getUserInfo(isSystem, userId);
    // }

}
