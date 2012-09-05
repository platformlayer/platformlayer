package org.platformlayer.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

public class UrlUtils {
	public static String urlEncode(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("UTF-8 not supported", e);
		}
	}

	public static String buildQueryString(Map<String, String> parameters) {
		StringBuilder queryString = new StringBuilder();
		for (Entry<String, String> parameter : parameters.entrySet()) {
			if (queryString.length() != 0) {
				queryString.append('&');
			}
			queryString.append(urlEncode(parameter.getKey()));
			queryString.append('=');
			queryString.append(urlEncode(parameter.getValue()));
		}
		return queryString.toString();
	}
}
