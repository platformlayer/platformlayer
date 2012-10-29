package org.platformlayer.xaas.web.jaxrs;

import javax.inject.Inject;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;

@Provider
public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {
	final ObjectMapper objectMapper;

	@Inject
	public ObjectMapperProvider(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public ObjectMapper getContext(Class<?> type) {
		return objectMapper;
	}
}
