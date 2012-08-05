package org.openstack.keystone.resources.user;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.log4j.Logger;
import org.platformlayer.CustomerFacingException;
import org.platformlayer.auth.OpsUser;
import org.platformlayer.auth.UserEntity;
import org.platformlayer.auth.model.RegistrationRequest;
import org.platformlayer.auth.model.RegistrationResponse;
import org.platformlayer.auth.services.RegistrationService;

@Path("/v2.0/register")
public class RegisterResource extends UserResourceBase {
	static final Logger log = Logger.getLogger(RegisterResource.class);

	@Inject
	RegistrationService registrationService;

	@POST
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	@Consumes({ APPLICATION_JSON, APPLICATION_XML })
	public RegistrationResponse doRegister(RegistrationRequest request) {
		RegistrationResponse response = register(request);

		return response;
	}

	private RegistrationResponse register(RegistrationRequest request) {
		RegistrationResponse response = new RegistrationResponse();

		String username = request.username;
		String password = request.password;

		UserEntity userEntity;
		try {
			OpsUser user = registrationService.registerUser(username, password);
			userEntity = (UserEntity) user;
		} catch (CustomerFacingException e) {
			response.errorMessage = e.getMessage();
			return response;
		}

		if (userEntity == null) {
			log.warn("Authentication request failed immediately after registration.  Username=" + username);
			throw new IllegalStateException();
		}

		response.access = buildAccess(userEntity);
		return response;
	}

}
