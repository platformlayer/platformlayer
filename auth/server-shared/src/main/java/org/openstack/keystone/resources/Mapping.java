package org.openstack.keystone.resources;

import java.util.List;

import org.openstack.keystone.model.Role;
import org.openstack.keystone.model.RoleList;
import org.openstack.keystone.model.Tenant;
import org.openstack.keystone.model.Token;
import org.openstack.keystone.model.User;
import org.openstack.keystone.services.TokenInfo;
import org.openstack.keystone.services.UserInfo;

public class Mapping {

	public static Token mapToResponse(TokenInfo tokenInfo) {
		Token token = new Token();
		token.expires = tokenInfo.expiration;
		if (tokenInfo.scope != null) {
			token.tenant = mapToTenant(tokenInfo);
		}

		return token;
	}

	private static Tenant mapToTenant(TokenInfo tokenInfo) {
		Tenant tenant = new Tenant();
		tenant.id = tokenInfo.scope;
		tenant.name = tokenInfo.scope;
		return tenant;
	}

	public static RoleList mapToRoles(List<Role> roles) {
		RoleList roleList = new RoleList();
		roleList.roles = roles;
		return roleList;
	}

	public static User mapToUser(UserInfo userInfo) {
		User user = new User();
		user.enabled = true;
		user.id = userInfo.userId;
		user.username = userInfo.username;
		user.email = userInfo.email;
		return user;
	}

}
