package org.platformlayer.auth.services;

import org.platformlayer.CustomerFacingException;
import org.platformlayer.auth.OpsUser;
import org.platformlayer.auth.services.registration.RegistrationServiceImpl;

import com.google.inject.ImplementedBy;

@ImplementedBy(RegistrationServiceImpl.class)
public interface RegistrationService {
	public OpsUser registerUser(String username, String password) throws CustomerFacingException;
}
