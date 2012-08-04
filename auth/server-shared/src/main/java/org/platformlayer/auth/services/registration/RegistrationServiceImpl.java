package org.platformlayer.auth.services.registration;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.platformlayer.CustomerFacingException;
import org.platformlayer.RepositoryException;
import org.platformlayer.auth.OpsUser;
import org.platformlayer.auth.UserDatabase;
import org.platformlayer.auth.services.RegistrationService;

import com.google.common.base.Strings;

@Singleton
public class RegistrationServiceImpl implements RegistrationService {
	static final Logger log = Logger.getLogger(RegistrationServiceImpl.class);

	private static final int MIN_PASSWORD_LENGTH = 6;

	@Inject
	UserDatabase repository;

	@Override
	public OpsUser registerUser(String username, String password) throws CustomerFacingException {
		if (Strings.isNullOrEmpty(username)) {
			throw new CustomerFacingException("Username is required");
		}

		if (Strings.isNullOrEmpty(password)) {
			throw new CustomerFacingException("Password is required");
		}

		password = password.trim();
		username = username.trim();

		checkPassword(password);

		checkUsername(username);

		OpsUser user;
		try {
			user = repository.findUser(username);
			if (user != null) {
				// TODO: Should we hide this fact?
				throw new CustomerFacingException("Username is already registered");
			}

			user = repository.createUser(username, password, null);

			// TODO: We reserve @@, to prevent collisions
			// TODO: Is this good enough? What if project already exists?
			String projectKey = "user@@" + username.toLowerCase();
			repository.createProject(projectKey, user);
		} catch (RepositoryException e) {
			log.warn("Repository error creating user", e);
			throw new CustomerFacingException("Internal error - please try again later", e);
		}

		return user;
	}

	private void checkUsername(String email) throws CustomerFacingException {
		if (!email.contains("@")) {
			throw new CustomerFacingException("Email address is invalid");
		}
		// TODO: More verification
	}

	private void checkPassword(String password) throws CustomerFacingException {
		if (password.length() < MIN_PASSWORD_LENGTH) {
			throw new CustomerFacingException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
		}
		// TODO: Minimum complexity?
	}
}
