package org.platformlayer.xaas.web.jaxrs;

public class NamespacePrefixMapperImpl extends com.sun.xml.bind.marshaller.NamespacePrefixMapper {
	@Override
	public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
		if (namespaceUri.equals("http://platformlayer.org/core/v1.0")) {
			return "core";
		}

		String stripped = namespaceUri;

		if (stripped.startsWith("http://platformlayer.org/service/")) {
			stripped = stripped.substring("http://platformlayer.org/service/".length());
		}

		if (stripped.endsWith("/v1.0")) {
			stripped = stripped.substring(0, stripped.length() - "/v1.0".length());
		}

		if (stripped.contains("/")) {
			return suggestion;
		}

		return stripped;
	}
}
