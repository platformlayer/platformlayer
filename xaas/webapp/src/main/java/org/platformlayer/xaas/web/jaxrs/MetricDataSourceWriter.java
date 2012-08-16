package org.platformlayer.xaas.web.jaxrs;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.inject.Singleton;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.platformlayer.metrics.model.MetricDataSource;
import org.platformlayer.web.ResourceBase;

@Singleton
@Produces({ ResourceBase.JSON, ResourceBase.XML })
@Provider
public class MetricDataSourceWriter implements MessageBodyWriter<MetricDataSource> {

	@Override
	public long getSize(MetricDataSource metricData, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType) {
		return -1;
	}

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return MetricDataSource.class.isAssignableFrom(type);
	}

	@Override
	public void writeTo(MetricDataSource metricData, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> headers, OutputStream out) throws IOException,
			WebApplicationException {
		if (mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
			metricData.serializeAsJson(out);
		} else if (mediaType.isCompatible(MediaType.APPLICATION_XML_TYPE)) {
			metricData.serializeAsXml(out);
		} else {
			throw new UnsupportedOperationException();
		}
	}
}
