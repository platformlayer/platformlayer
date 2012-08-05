package org.platformlayer.web;

import java.io.IOException;

import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * Very simple CORS filter
 * 
 */
@Singleton
public class CORSFilter implements Filter {

	// TODO: Should we use this instead? http://software.dzhuvinov.com/cors-filter-installation.html

	private static final String HEADER_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
	private static final String HEADER_ALLOW_HEADERS = "Access-Control-Allow-Headers";

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		httpResponse.addHeader(HEADER_ALLOW_ORIGIN, "*");
		httpResponse.addHeader(HEADER_ALLOW_HEADERS, "Content-Type, X-Auth-Token");

		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
	}

}