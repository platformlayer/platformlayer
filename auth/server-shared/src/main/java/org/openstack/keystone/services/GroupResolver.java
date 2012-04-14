package org.openstack.keystone.services;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.Sets;

public class GroupResolver {
	// Prevent infinite recursion if we have a pathological structure
	private static final int MAX_DEPTH = 128;

	static final Logger log = Logger.getLogger(GroupResolver.class);

	Set<String> foundGroups = Sets.newHashSet();

	final GroupMembershipOracle groupMembership;

	public GroupResolver(GroupMembershipOracle groupMembership) {
		this.groupMembership = groupMembership;
	}

	public Collection<String> findGroups(String userId) throws AuthenticatorException {
		visit(userId, 0);

		return foundGroups;
	}

	void visit(String entity, int depth) throws AuthenticatorException {
		if (depth == 0) {
			// Don't add the first entity; it's a user
		} else {
			if (depth >= MAX_DEPTH) {
				log.warn("Max recursion depth encountered; won't descend further");
				return;
			}
		}

		List<String> groups = groupMembership.getGroups(entity, depth != 0);
		for (String group : groups) {
			if (foundGroups.contains(group)) {
				// Already visited
				continue;
			} else {
				foundGroups.add(group);
				visit(group, depth + 1);
			}
		}
	}
}
