package org.openstack.keystone.resources.user;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.log4j.Logger;
import org.openstack.keystone.model.Ec2ConvertTokenRequest;
import org.openstack.keystone.model.Ec2ConvertTokenResponse;
import org.openstack.keystone.resources.KeystoneResourceBase;

@Path("/v2.0/ec2tokens")
public class Ec2TokensResource extends KeystoneResourceBase {
    static final Logger log = Logger.getLogger(Ec2TokensResource.class);

    @POST
    @Produces({ APPLICATION_JSON, APPLICATION_XML })
    @Consumes({ APPLICATION_JSON, APPLICATION_XML })
    public Ec2ConvertTokenResponse convertToken(Ec2ConvertTokenRequest request) {
        throw new UnsupportedOperationException();

        //
        // boolean isSystem = false;
        // TokenInfo tokenInfo = tryAuthenticate(isSystem, request.auth);
        //
        // if (tokenInfo == null) {
        // throwUnauthorized();
        // }
        //
        // String scope = request.auth.tenantName;
        //
        // UserInfo userInfo = authentication.getUserInfo(isSystem, tokenInfo.userId);
        //
        // AuthenticateResponse response = new AuthenticateResponse();
        // response.access = new Access();
        // response.access.serviceCatalog = authentication.getServices(userInfo, scope);
        //
        // if (scope != null) {
        // // If we are doing a scope auth, make sure we have access
        // if (isNullOrEmpty(response.access.serviceCatalog)) {
        // throwUnauthorized();
        // }
        // }
        //
        // log.debug("Successful authentication for user: " + tokenInfo.userId);
        //
        // response.access.token = Mapping.mapToResponse(tokenInfo);
        // response.access.token.id = authentication.signToken(tokenInfo);
        //
        // return response;
    }
}
