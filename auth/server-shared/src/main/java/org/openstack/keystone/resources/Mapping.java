package org.openstack.keystone.resources;

import java.util.List;

import org.openstack.keystone.model.ProjectValidation;
import org.openstack.keystone.model.Role;
import org.openstack.keystone.model.RoleList;
import org.openstack.keystone.model.User;
import org.openstack.keystone.model.UserValidation;
import org.platformlayer.auth.ProjectEntity;
import org.platformlayer.auth.UserEntity;
import org.platformlayer.crypto.AesUtils;

public class Mapping {

	public static RoleList mapToRoles(List<Role> roles) {
		RoleList roleList = new RoleList();
		roleList.roles = roles;
		return roleList;
	}

	public static User mapToUser(UserEntity userInfo) {
		User user = new User();
		user.enabled = true;
		user.id = "" + userInfo.id;
		user.username = userInfo.key;
		user.email = userInfo.key;
		return user;
	}

	public static UserValidation mapToUserValidation(UserEntity userInfo) {
		UserValidation user = new UserValidation();
		user.id = "" + userInfo.id;
		user.name = userInfo.key;
		user.secret = AesUtils.serialize(userInfo.getUserSecret());
		return user;
	}

	public static ProjectValidation mapToProject(ProjectEntity entity) {
		ProjectValidation mapped = new ProjectValidation();
		mapped.id = "" + entity.id;
		mapped.name = entity.name;
		mapped.secret = AesUtils.serialize(entity.getProjectSecret());
		return mapped;
	}

}
