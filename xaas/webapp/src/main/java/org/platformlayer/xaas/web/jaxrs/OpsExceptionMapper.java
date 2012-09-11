package org.platformlayer.xaas.web.jaxrs;

import javax.inject.Singleton;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.platformlayer.CustomerFacingException;
import org.platformlayer.core.model.ErrorDetail;
import org.platformlayer.core.model.ErrorResponse;
import org.platformlayer.ops.OpsException;

import com.sun.jersey.api.core.HttpContext;

@Singleton
@Provider
public class OpsExceptionMapper implements ExceptionMapper<OpsException> {

	@Context
	HttpContext httpContext;

	@Override
	public Response toResponse(OpsException e) {
		ErrorResponse error = new ErrorResponse();
		error.message = e.getMessage();

		if (e instanceof CustomerFacingException) {
			CustomerFacingException cfe = (CustomerFacingException) e;
			error.code = cfe.getCode();

			for (CustomerFacingException.Info info : cfe.getInfo()) {
				ErrorDetail errorInfo = new ErrorDetail();
				errorInfo.code = info.getCode();
				errorInfo.field = info.getField();
				errorInfo.message = info.getMessage();

				error.details.add(errorInfo);
			}
		}

		if (error.code == null) {
			error.code = e.getClass().getSimpleName();
		}

		ResponseBuilder response = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
		response.entity(error);
		return response.build();

		// VariantListBuilder variantListBuilder = VariantListBuilder.newInstance();
		// variantListBuilder.mediaTypes(MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_XML_TYPE);
		// List<Variant> variants = variantListBuilder.build();
		//
		// Variant variant = httpContext.getRequest().selectVariant(variants);
		//
		// ResponseBuilder response = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
		//
		// if (variant.getMediaType().equals(MediaType.APPLICATION_JSON_TYPE)) {
		// response.entity(error);
		//
		// .entity(exception.getMessage()).type("text/plain").build();
		// }
		// else {
		// .entity(exception.getMessage()).type("text/plain").build();
		// }
		//
		// return response.build();
	}
}
