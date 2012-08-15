package org.platformlayer.auth;

import java.util.Collections;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.platformlayer.model.RoleId;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

@Entity
@Table(name = "user_projects")
public class UserProjectEntity {

	@Column(name = "user_id")
	public int userId;

	@Column(name = "project_id")
	public int projectId;

	@Column(name = "roles")
	String joinedRoles;

	public List<RoleId> getRoles() {
		List<RoleId> ret = Lists.newArrayList();
		if (joinedRoles != null) {
			for (String key : Splitter.on(",").split(joinedRoles)) {
				ret.add(new RoleId(key));
			}
		}
		return ret;
	}

	public void setRoles(List<RoleId> roles) {
		StringBuilder sb = new StringBuilder();
		for (RoleId role : roles) {
			if (sb.length() != 0) {
				sb.append(",");
			}
			sb.append(role.getKey());
		}
		this.joinedRoles = sb.toString();
	}

	public void addRole(RoleId role) {
		addRoles(Collections.singletonList(role));
	}

	public void addRoles(List<RoleId> addRoles) {
		List<RoleId> roles = getRoles();
		roles.addAll(addRoles);
		setRoles(roles);
	}
}
