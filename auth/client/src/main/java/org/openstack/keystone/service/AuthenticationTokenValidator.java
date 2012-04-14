package org.openstack.keystone.service;

import org.platformlayer.model.Authentication;

public interface AuthenticationTokenValidator {
	Authentication validate(String authToken);
}
