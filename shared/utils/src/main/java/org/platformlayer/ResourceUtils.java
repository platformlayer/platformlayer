package org.platformlayer;

import java.io.IOException;
import java.io.InputStream;

import org.platformlayer.ops.OpsException;
import org.platformlayer.xml.JaxbHelper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

public class ResourceUtils {
	public static String get(Class<?> contextClass, String resourceName) throws IOException {
		String value = find(contextClass, resourceName);
		if (value == null) {
			throw new IllegalArgumentException("Cannot find resource: " + resourceName);
		}
		return value;
	}

	public static String find(Class<?> contextClass, String resourceName) throws IOException {
		InputStream resourceAsStream = null;
		try {
			resourceAsStream = contextClass.getResourceAsStream(resourceName);
			if (resourceAsStream == null) {
				return null;
			}

			return IoUtils.readAll(resourceAsStream);
		} finally {
			IoUtils.safeClose(resourceAsStream);
		}
	}

	public static byte[] getBinary(Class<?> contextClass, String resourceName) throws IOException {
		byte[] value = findBinary(contextClass, resourceName);
		if (value == null) {
			throw new IllegalArgumentException("Cannot find resource: " + resourceName);
		}
		return value;
	}

	public static byte[] findBinary(Class<?> contextClass, String resourceName) throws IOException {
		InputStream resourceAsStream = null;
		try {
			resourceAsStream = contextClass.getResourceAsStream(resourceName);
			if (resourceAsStream == null) {
				return null;
			}

			return IoUtils.readAllBinary(resourceAsStream);
		} finally {
			IoUtils.safeClose(resourceAsStream);
		}
	}

	public static String makeAbsoluteResource(Class<?> contextClass, String relativePath) {
		String resourcePath;
		if (!relativePath.contains("/")) {
			resourcePath = contextClass.getPackage().getName().replace('.', '/') + "/" + relativePath;
		} else {
			resourcePath = relativePath;
		}
		return resourcePath;
	}

	public static <T> T findResource(Class<?> contextClass, String key, Class<T> clazz) throws OpsException {
		{
			String resourceName = key + ".xml";
			try {
				String xml = ResourceUtils.find(contextClass, resourceName);
				if (xml != null) {
					return JaxbHelper.deserializeXmlObject(xml, clazz);
				}
			} catch (Exception e) {
				throw new OpsException("Error reading resource: " + resourceName, e);
			}
		}

		{
			String resourceName = key + ".json";
			try {
				String json = ResourceUtils.find(contextClass, resourceName);
				if (json != null) {
					ObjectMapper mapper = new ObjectMapper();

					// Use JAXB annotations
					AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
					mapper = mapper.setAnnotationIntrospector(introspector);

					mapper = mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
					mapper = mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
					mapper = mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);

					if (!json.startsWith("{")) {
						// Be tolerant
						json = "{ " + json + "}";
						// json = jsonHelper.wrapJson(json);
					}

					return mapper.readValue(json, clazz);
					// return jsonHelper.unmarshal(json);
				}
			} catch (Exception e) {
				throw new OpsException("Error reading resource: " + resourceName, e);
			}
		}

		return null;
	}

}
