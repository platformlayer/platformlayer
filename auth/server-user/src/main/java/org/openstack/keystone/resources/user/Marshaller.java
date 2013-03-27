package org.openstack.keystone.resources.user;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.inject.Singleton;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.platformlayer.xml.JaxbHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;

@Singleton
public class Marshaller {

	private static final Logger log = LoggerFactory.getLogger(Marshaller.class);

	final ObjectMapper jsonMapper = buildJsonMapper();

	private ObjectMapper buildJsonMapper() {
		ObjectMapper mapper = new ObjectMapper();
		return mapper;
	}

	public <T> T read(HttpServletRequest httpRequest, Class<T> clazz) {
		InputStream is;
		try {
			is = httpRequest.getInputStream();
		} catch (IOException e) {
			log.info("Error deserializing value", e);
			return null;
		}

		// // We do gzip compression directly (for now)
		// String ce = httpRequest.getHeader("content-encoding");
		// if (ce != null) {
		// if (ce.equalsIgnoreCase("gzip")) {
		// is = new GZIPInputStream(is);
		// } else {
		// httpRequest.sendError(415);
		// return;
		// }
		// }

		String contentType = httpRequest.getHeader("content-type");
		if (contentType == null) {
			contentType = "";
		} else {
			contentType = contentType.toLowerCase().trim();
		}
		boolean json = true;

		if (contentType.equals("application/xml")) {
			json = false;
		}

		if (json) {
			try {
				T t = jsonMapper.readValue(is, clazz);
				return t;
			} catch (Exception e) {
				log.info("Error deserializing value", e);
				return null;
			}
		} else {
			try {
				JaxbHelper jaxb = JaxbHelper.get(clazz);
				T t = (T) jaxb.deserializeXmlObject(is, clazz, false);
				return t;
			} catch (Exception e) {
				log.info("Error deserializing value", e);
				return null;
			}
		}

	}

	public <T> void write(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, T response) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// // We do gzip compression directly (for now)
		// String ce = httpRequest.getHeader("content-encoding");
		// if (ce != null) {
		// if (ce.equalsIgnoreCase("gzip")) {
		// is = new GZIPInputStream(is);
		// } else {
		// httpRequest.sendError(415);
		// return;
		// }
		// }

		String accept = httpRequest.getHeader("accept");
		if (accept == null) {
			accept = "";
		}
		boolean json = true;

		for (String acceptType : Splitter.on(',').split(accept)) {
			int semiIndex = acceptType.indexOf(';');
			if (semiIndex != -1) {
				acceptType = acceptType.substring(0, semiIndex);
			}
			acceptType = acceptType.trim().toLowerCase();
			if (accept.equals("application/xml")) {
				json = false;
				break;
			} else if (accept.equals("application/json")) {
				json = true;
				break;
			}
		}

		if (json) {
			try {
				jsonMapper.writeValue(baos, response);
			} catch (Exception e) {
				log.error("Error serializing value", e);
				httpResponse
						.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
		} else {
			try {
				JaxbHelper jaxb = JaxbHelper.get(response.getClass());
				boolean formatted = false;
				jaxb.marshal(response, formatted, baos);
			} catch (Exception e) {
				log.error("Error serializing value", e);
				httpResponse
						.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
		}

		byte[] data = baos.toByteArray();
		if (json) {
			httpResponse.setContentType("application/json");
		} else {
			httpResponse.setContentType("application/xml");
		}
		httpResponse.setContentLength(data.length);

		try {
			ServletOutputStream os = httpResponse.getOutputStream();
			os.write(data);
			os.flush();
		} catch (IOException e) {
			// Not a lot we can do here ... we've already started sending data
			log.error("Error flushing data", e);
			// Try setting an error response
			try {
				httpResponse
						.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			} catch (Exception e2) {
				log.warn("Unable to set error status", e2);
			}
			return;
		}
	}

}
