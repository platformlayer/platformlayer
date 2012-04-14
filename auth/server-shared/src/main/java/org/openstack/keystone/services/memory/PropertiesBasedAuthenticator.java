package org.openstack.keystone.services.memory;

import java.util.Properties;

import org.openstack.keystone.services.AuthenticationInfo;
import org.openstack.keystone.services.GenericAuthenticator;
import org.openstack.keystone.services.GroupMembershipOracle;
import org.platformlayer.crypto.SecureComparison;

public class PropertiesBasedAuthenticator implements GenericAuthenticator {
	final Properties properties;
	final GroupMembershipOracle groupMembership;

	public PropertiesBasedAuthenticator(Properties properties) {
		super();
		this.properties = properties;
		this.groupMembership = new PropertiesBasedGroupMembership(properties);
	}

	@Override
	public AuthenticationInfo authenticate(String username, String password) {
		String prefix = username;

		String passwordProperty = properties.getProperty(prefix + ".password");
		if (passwordProperty == null) {
			return null;
		}

		if (passwordProperty.startsWith("md5:")) {
			// We could easily support hashed passwords
			throw new UnsupportedOperationException();
		} else {
			if (!SecureComparison.equal(passwordProperty, password)) {
				return null;
			}
		}

		String userId = username;
		byte[] tokenSecret = null;
		AuthenticationInfo authenticated = new AuthenticationInfo(userId, tokenSecret);
		return authenticated;
	}

	@Override
	public GroupMembershipOracle getGroupMembership() {
		return groupMembership;
	}

	@Override
	public byte[] getUserSecret(String userId, byte[] tokenSecret) {
		return null;
	}
}
