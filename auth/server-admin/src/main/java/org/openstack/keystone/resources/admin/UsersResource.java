//package org.openstack.keystone.resources.admin;
//
//import javax.ws.rs.GET;
//import javax.ws.rs.Path;
//import javax.ws.rs.Produces;
//import javax.ws.rs.QueryParam;
//
//import org.apache.log4j.Logger;
//import org.openstack.keystone.model.User;
//import org.openstack.keystone.resources.KeystoneResourceBase;
//import org.openstack.keystone.resources.Mapping;
//import org.openstack.keystone.services.AuthenticatorException;
//import org.openstack.keystone.services.UserInfo;
//
//@Path("users")
//public class UsersResource extends KeystoneResourceBase {
//    static final Logger log = Logger.getLogger(UsersResource.class);
//
//    @GET
//    @Path("{userId}/roles")
//    @Produces({ APPLICATION_XML, APPLICATION_JSON })
//    public RoleList getUserRoles(@PathParam("userId") String userId) {
//        requireSystemToken();
//
//        boolean isSystemUser = false;
//        UserInfo userInfo = null;
//        try {
//            userInfo = authentication.getUserInfo(isSystemUser, userId);
//        } catch (AuthenticatorException e) {
//            // An exception indicates something went wrong (i.e. not just bad credentials)
//            log.warn("Error while getting user info", e);
//            throwInternalError();
//        }
//
//        if (userInfo == null) {
//            throw404NotFound();
//        }
//
//        List<Role> roles = authentication.getRoles(userInfo, null);
//        List<Role> globalRoles = Lists.newArrayList();
//        for (Role role : roles) {
//            if (role.tenantId != null)
//                continue;
//            globalRoles.add(role);
//        }
//        return Mapping.mapToRoles(globalRoles);
//    }
//
//    @GET
//    @Path("{userId}")
//    @Produces({ APPLICATION_XML, APPLICATION_JSON })
//    public User getUserById(@PathParam("userId") String userId) {
//        requireSystemToken();
//
//        boolean isSystemUser = false;
//
//        UserInfo userInfo = null;
//        try {
//            userInfo = authentication.getUserInfo(isSystemUser, userId);
//        } catch (AuthenticatorException e) {
//            // An exception indicates something went wrong (i.e. not just bad credentials)
//            log.warn("Error while getting user info", e);
//            throwInternalError();
//        }
//        if (userInfo == null) {
//            throw404NotFound();
//        }
//
//        return Mapping.mapToUser(userInfo);
//    }
//
//    @GET
//    @Produces({ APPLICATION_XML, APPLICATION_JSON })
//    public User getUserByUsername(@QueryParam("username") String username) {
//        requireSystemToken();
//
//        boolean isSystemUser = false;
//
//        UserInfo userInfo = null;
//        try {
//            userInfo = authentication.getUserInfoByUsername(isSystemUser, username);
//        } catch (AuthenticatorException e) {
//            // An exception indicates something went wrong (i.e. not just bad credentials)
//            log.warn("Error while getting user info", e);
//            throwInternalError();
//        }
//
//        if (userInfo == null) {
//            throw404NotFound();
//        }
//
//        return Mapping.mapToUser(userInfo);
//    }
//
// }
