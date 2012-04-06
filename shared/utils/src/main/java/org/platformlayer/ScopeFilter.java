package org.platformlayer;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ScopeFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        Scope scope = Scope.inherit();

        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

            scope.put(HttpServletRequest.class, httpServletRequest);
            scope.put(HttpServletResponse.class, httpServletResponse);
        } else {
            scope.put(HttpServletRequest.class, null);
            scope.put(HttpServletResponse.class, null);
        }

        try {
            scope.push();

            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            scope.pop();
        }
    }

    @Override
    public void destroy() {

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }
}
