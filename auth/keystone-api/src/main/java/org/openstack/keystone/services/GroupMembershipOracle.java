package org.openstack.keystone.services;

import java.util.List;

public interface GroupMembershipOracle {
    List<String> getGroups(String key, boolean isGroup) throws AuthenticatorException;
}
