package org.platformlayer.xaas.web;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.platformlayer.TimeSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GwtCacheHeaderFilter implements Filter {
	private static final Logger log = LoggerFactory.getLogger(GwtCacheHeaderFilter.class);

	public static final String DATE_PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		if (response instanceof HttpServletResponse) {
			String requestUri = ((HttpServletRequest) request).getRequestURI();

			HttpServletResponse httpServletResponse = (HttpServletResponse) response;

			TimeSpan cacheFor = null;

			if (requestUri.contains(".cache.")) {
				// Cache forever
				cacheFor = TimeSpan.ONE_DAY.multiplyBy(365);
			} else if (requestUri.contains(".nocache.")) {
				// Don't ever cache
				cacheFor = null;
			} else {
				// Micro cache
				cacheFor = TimeSpan.TEN_MINUTES;
			}

			if (cacheFor == null) {
				httpServletResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
				httpServletResponse.setHeader("Expires", "0");
			} else {
				long now = System.currentTimeMillis() / 1000L;
				long timeoutAt = now + cacheFor.getTotalSeconds();

				long deltaSeconds = timeoutAt - now;

				httpServletResponse.setHeader("Cache-Control", "max-age=" + deltaSeconds);

				SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN_RFC1123);
				String expires = dateFormat.format(new Date(timeoutAt * 1000L));
				httpServletResponse.setHeader("Expires", expires);
			}
		}
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {

	}

}
