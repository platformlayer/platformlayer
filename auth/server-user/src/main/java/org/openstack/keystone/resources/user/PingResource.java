package org.openstack.keystone.resources.user;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/api/ping")
public class PingResource {
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public String ping() {
		return "{ 'message': 'pong' }";
	}

}
