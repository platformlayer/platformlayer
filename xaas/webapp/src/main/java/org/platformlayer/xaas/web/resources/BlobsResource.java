package org.platformlayer.xaas.web.resources;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.platformlayer.ops.OneTimeDownloads;
import org.platformlayer.ops.OneTimeDownloads.Download;

public class BlobsResource {

	@Inject
	OneTimeDownloads oneTimeDownloads;

	@GET
	@Path("blob/{blobId}")
	public Response downloadBlob(@PathParam("blobId") String blobId) {
		Download download = oneTimeDownloads.get(blobId);
		if (download == null) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}

		String responseData = download.getContent();
		String contentType = download.getContentType();

		ResponseBuilder rBuild = Response.ok(responseData, contentType);
		return rBuild.build();
	}
}
