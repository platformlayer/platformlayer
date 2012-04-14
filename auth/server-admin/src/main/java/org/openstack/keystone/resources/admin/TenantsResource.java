package org.openstack.keystone.resources.admin;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.openstack.keystone.model.Tenant;
import org.openstack.keystone.model.TenantsList;
import org.openstack.keystone.resources.KeystoneResourceBase;

public class TenantsResource extends KeystoneResourceBase {
	@GET
	@Produces({ APPLICATION_XML, APPLICATION_JSON })
	public TenantsList listTenants(@QueryParam("name") String tenantName) {
		throw new UnsupportedOperationException();
		// TokenInfo myToken = requireAdminToken();
		//
		// if (tenantName != null) {
		// // SPECBUG: Calls should always return the same schema
		// throw new UnsupportedOperationException();
		// }
		//
		// TenantsList tenants = authentication.listTenants(myToken, null);
		// return tenants;
	}

	@GET
	@Path("{tenantId}/users/{userId}/roles")
	@Produces({ APPLICATION_XML, APPLICATION_JSON })
	public void getRoles(@PathParam("tenantId") String tenantId, @PathParam("userId") String userId) {
		throw new UnsupportedOperationException();

		// TokenInfo myToken = requireAdminToken();
		//
		// TenantsList tenants = authentication.listTenants(myToken, null);
		// return tenants;
	}

	@GET
	@Path("{tenantId}/endpoints")
	@Produces({ APPLICATION_XML, APPLICATION_JSON })
	public void getEndpoints(@HeaderParam("X-Auth-Token") String tokenId, @PathParam("tenantId") String tenantId) {
		throw new UnsupportedOperationException();
	}

	@GET
	@Path("{tenantId}")
	@Produces({ APPLICATION_XML, APPLICATION_JSON })
	public Tenant getTenant(@PathParam("tenantId") String tenantId) {
		throw new UnsupportedOperationException();

		// TokenInfo myToken = requireAdminToken();
		//
		// TenantsList tenants = authentication.listTenants(myToken, tenantId);
		// if (isNullOrEmpty(tenants.tenant)) {
		// throw404NotFound();
		// }
		// if (tenants.tenant.size() != 1) {
		// throw new IllegalStateException("Unexpected number of items found");
		// }
		// return tenants.tenant.get(0);
	}
}
