package org.platformlayer.ui.web.server;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.google.inject.Injector;

public class ServerContextFilter implements Filter {

    @Inject
    Injector injector;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ServerContext serverContext = injector.getInstance(ServerContext.class);
        ServerContext.set(serverContext);
        try {
            chain.doFilter(request, response);
        } finally {
            ServerContext.set(null);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void destroy() {

    }
}
