package org.platformlayer.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class HttpUtils {
	public static String urlEncode(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("UTF-8 not supported", e);
		}
	}
}
