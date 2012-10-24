package org.platformlayer.auth.resources;

import java.util.List;

import org.platformlayer.auth.ProjectEntity;
import org.platformlayer.auth.UserEntity;
import org.platformlayer.auth.model.ProjectValidation;
import org.platformlayer.auth.model.Role;
import org.platformlayer.auth.model.User;
import org.platformlayer.auth.model.UserValidation;
import org.platformlayer.model.RoleId;

import com.fathomdb.crypto.FathomdbCrypto;
import com.google.common.collect.Lists;

public class Mapping {

	// public static RoleList mapToRoles(List<Role> roles) {
	// RoleList roleList = new RoleList();
	// roleList.roles = roles;
	// return roleList;
	// }

	public static List<Role> mapToRoles(List<RoleId> roles) {
		List<Role> roleList = Lists.newArrayList();
		for (RoleId role : roles) {
			Role xmlRole = new Role();
			xmlRole.name = role.getKey();
			roleList.add(xmlRole);
		}
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
		// user.secret = AesUtils.serialize(userInfo.getUserSecret());
		return user;
	}

	public static ProjectValidation mapToProject(ProjectEntity entity) {
		ProjectValidation mapped = new ProjectValidation();
		mapped.id = "" + entity.id;
		mapped.name = entity.name;
		if (!entity.isLocked()) {
			mapped.secret = FathomdbCrypto.serialize(entity.getProjectSecret());
		}
		return mapped;
	}

}
