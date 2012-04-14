package org.openstack.keystone.services.memory;

import java.util.List;
import java.util.Properties;

import org.openstack.keystone.services.GroupMembershipOracle;

import com.google.common.collect.Lists;

public class PropertiesBasedGroupMembership implements GroupMembershipOracle {
	final Properties properties;

	public PropertiesBasedGroupMembership(Properties properties) {
		super();
		this.properties = properties;
	}

	@Override
	public List<String> getGroups(String key, boolean isGroup) {
		String prefix;

		if (isGroup) {
			prefix = "group." + key;
		} else {
			prefix = "user." + key;
		}

		List<String> groups = Lists.newArrayList();

		String groupsString = properties.getProperty(prefix + ".groups");
		if (groupsString != null) {
			for (String group : groupsString.split(",")) {
				groups.add(group);
			}
		}

		return groups;
	}
}
