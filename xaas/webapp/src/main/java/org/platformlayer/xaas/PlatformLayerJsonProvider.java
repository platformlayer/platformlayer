package org.platformlayer.xaas;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

@Provider
@Consumes(MediaType.WILDCARD)
// NOTE: required to support "non-standard" JSON variants
@Produces(MediaType.WILDCARD)
public class PlatformLayerJsonProvider extends JacksonJaxbJsonProvider {
	@Inject
	public PlatformLayerJsonProvider(ObjectMapper objectMapper) {
		super(objectMapper, JacksonJaxbJsonProvider.DEFAULT_ANNOTATIONS);
	}

}
