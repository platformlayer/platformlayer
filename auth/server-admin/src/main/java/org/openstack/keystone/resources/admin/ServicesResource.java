package org.openstack.keystone.resources.admin;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.platformlayer.auth.AuthenticatorException;
import org.platformlayer.auth.ServiceAccountEntity;
import org.platformlayer.auth.model.CheckServiceAccessRequest;
import org.platformlayer.auth.model.CheckServiceAccessResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/services")
public class ServicesResource extends RootResource {
	private static final Logger log = LoggerFactory.getLogger(ServicesResource.class);

	@POST
	@Path("check")
	public CheckServiceAccessResponse checkServiceAccess(CheckServiceAccessRequest request) {
		try {
			requireSystemAccess();
		} catch (AuthenticatorException e) {
			log.warn("Error while checking system token", e);
			throwInternalError();
		}

		ServiceAccountEntity serviceAccount = null;
		try {
			serviceAccount = systemAuthenticator.authenticate(request.chain);
		} catch (AuthenticatorException e) {
			log.warn("Error while authenticating chain", e);
			throwInternalError();
		}
		CheckServiceAccessResponse response = new CheckServiceAccessResponse();
		if (serviceAccount != null) {
			response.serviceAccount = serviceAccount.subject;
		}
		return response;
	}

}
