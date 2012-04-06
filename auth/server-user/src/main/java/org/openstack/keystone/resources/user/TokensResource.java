package org.openstack.keystone.resources.user;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.log4j.Logger;
import org.openstack.keystone.model.Access;
import org.openstack.keystone.model.AuthenticateRequest;
import org.openstack.keystone.model.AuthenticateResponse;
import org.openstack.keystone.resources.KeystoneResourceBase;
import org.openstack.keystone.resources.Mapping;
import org.openstack.keystone.server.ServiceMapper;
import org.openstack.keystone.services.AuthenticatorException;
import org.openstack.keystone.services.TokenInfo;
import org.openstack.keystone.services.UserInfo;

@Path("/v2.0/tokens")
public class TokensResource extends KeystoneResourceBase {
    static final Logger log = Logger.getLogger(TokensResource.class);

    @Inject
    ServiceMapper serviceMapper;

    @POST
    @Produces({ APPLICATION_JSON, APPLICATION_XML })
    @Consumes({ APPLICATION_JSON, APPLICATION_XML })
    public AuthenticateResponse authenticate(AuthenticateRequest request) {
        boolean isSystem = false;
        TokenInfo tokenInfo = null;
        try {
            tokenInfo = tryAuthenticate(isSystem, request.auth);
        } catch (Exception e) {
            // An exception indicates something went wrong (i.e. not just bad credentials)
            log.warn("Error while authenticating", e);
            throwInternalError();
        }

        if (tokenInfo == null) {
            throwUnauthorized();
        }

        String scope = request.auth.tenantName;

        UserInfo userInfo = null;
        try {
            userInfo = authentication.getUserInfo(isSystem, tokenInfo.userId, tokenInfo.tokenSecret);
        } catch (AuthenticatorException e) {
            // An exception indicates something went wrong (i.e. not just bad credentials)
            log.warn("Error while getting user info", e);
            throwInternalError();
        }

        AuthenticateResponse response = new AuthenticateResponse();
        response.access = new Access();
        response.access.serviceCatalog = serviceMapper.getServices(userInfo, scope);

        if (scope != null) {
            // If we are doing a scope auth, make sure we have access
            if (isNullOrEmpty(response.access.serviceCatalog)) {
                throwUnauthorized();
            }
        }

        log.debug("Successful authentication for user: " + tokenInfo.userId);

        response.access.token = Mapping.mapToResponse(tokenInfo);
        response.access.token.id = authentication.signToken(tokenInfo);

        return response;
    }

    // @GET
    // @Produces({ APPLICATION_JSON, APPLICATION_XML })
    // public TenantsList listTenants(@HeaderParam(AUTH_HEADER) String token) {
    // // TODO: What is this call for?
    //
    // TokenInfo tokenInfo = authentication.validateToken(token);
    // if (tokenInfo == null) {
    // throwUnauthorized();
    // }
    //
    // TenantsList response = new TenantsList();
    // response.tenant = Lists.newArrayList();
    //
    // String scope = tokenInfo.scope;
    // if (scope == null) {
    // // Unscoped token; no tenant access
    // } else {
    // Tenant tenant = new Tenant();
    // tenant.id = scope;
    // tenant.enabled = true;
    // tenant.name = scope;
    // response.tenant.add(tenant);
    // }
    //
    // return response;
    // }

}
