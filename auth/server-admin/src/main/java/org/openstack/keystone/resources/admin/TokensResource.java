package org.openstack.keystone.resources.admin;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;
import org.openstack.keystone.model.Access;
import org.openstack.keystone.model.Auth;
import org.openstack.keystone.model.UserValidation;
import org.openstack.keystone.model.ValidateAccess;
import org.openstack.keystone.model.ValidateTokenResponse;
import org.openstack.keystone.resources.KeystoneResourceBase;
import org.openstack.keystone.resources.Mapping;
import org.openstack.keystone.services.AuthenticatorException;
import org.openstack.keystone.services.TokenInfo;
import org.openstack.keystone.services.UserInfo;

import com.google.common.base.Objects;

@Path("v2.0/tokens")
public class TokensResource extends KeystoneResourceBase {
    static final Logger log = Logger.getLogger(TokensResource.class);

    @POST
    @Produces({ APPLICATION_XML, APPLICATION_JSON })
    @Consumes({ APPLICATION_XML, APPLICATION_JSON })
    public Access authenticate(Auth request) {
        throw new UnsupportedOperationException();

        // boolean isSystem = true;
        // TokenInfo tokenInfo = doAuthenticate(isSystem, request);
    }

    @GET
    // @HEAD support is automatic from the @GET
    @Path("{tokenId}")
    public ValidateTokenResponse validateToken(@HeaderParam("X-Auth-Token") String myToken, @PathParam("tokenId") String checkToken, @QueryParam("belongsTo") String tenantId) {
        requireSystemToken();

        TokenInfo checkTokenInfo = authentication.validateToken(false, checkToken);
        if (checkTokenInfo == null) {
            throw404NotFound();
        }

        if (tenantId != null) {
            if (!Objects.equal(tenantId, checkTokenInfo.scope))
                throw404NotFound();
        }

        UserInfo userInfo = null;
        try {
            userInfo = authentication.getUserInfo(checkTokenInfo.isSystem(), checkTokenInfo.userId, checkTokenInfo.tokenSecret);
        } catch (AuthenticatorException e) {
            log.warn("Error while listing groups", e);
            throwInternalError();
        }

        ValidateTokenResponse response = new ValidateTokenResponse();
        response.access = new ValidateAccess();
        response.access.user = new UserValidation();
        response.access.user.id = userInfo.userId;
        response.access.user.name = userInfo.username;
        response.access.user.roles = authentication.getRoles(userInfo, checkTokenInfo.scope);

        response.access.user.secret = userInfo.secret;

        response.access.token = Mapping.mapToResponse(checkTokenInfo);
        response.access.token.id = checkToken;

        return response;
    }

}
