package org.platformlayer.xaas.web;

import java.io.IOException;

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
public class CORSFilter implements Filter {

	// TODO: Should we use this instead? http://software.dzhuvinov.com/cors-filter-installation.html

	private static final String HEADER_ALLOW_ORIGIN = "Access-Control-Allow-Origin";

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		((HttpServletResponse) response).addHeader(HEADER_ALLOW_ORIGIN, "*");
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
	}

}