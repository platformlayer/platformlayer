package org.platformlayer.auth.jaxrs;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import javax.ws.rs.Produces;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

@Provider
@Produces("application/json")
public class JacksonConfigurator implements ContextResolver<ObjectMapper> {

	private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	private ObjectMapper mapper = new ObjectMapper();

	public JacksonConfigurator() {
		SerializationConfig serConfig = mapper.getSerializationConfig();
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

		serConfig.setDateFormat(dateFormat);

		DeserializationConfig deserializationConfig = mapper.getDeserializationConfig();
		deserializationConfig.setDateFormat(dateFormat);

		mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
	}

	@Override
	public ObjectMapper getContext(Class<?> arg0) {
		return mapper;
	}

}