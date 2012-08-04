package org.platformlayer.auth.services;

import org.platformlayer.CustomerFacingException;
import org.platformlayer.auth.OpsUser;

public interface RegistrationService {
	public OpsUser registerUser(String username, String password) throws CustomerFacingException;
}
