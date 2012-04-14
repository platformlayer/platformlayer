package org.platformlayer.xaas.web;

import java.util.EnumSet;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.DispatcherType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.platformlayer.ops.log.PerJobAppender;
import org.platformlayer.xaas.GuiceServletConfig;

import com.google.inject.servlet.GuiceFilter;

class StandaloneXaasWebserver {
	static final Logger log = Logger.getLogger(StandaloneXaasWebserver.class);

	static final int PORT = 8082;

	private Server server;

	public static void main(String[] args) throws Exception {
		StandaloneXaasWebserver server = new StandaloneXaasWebserver();
		server.start();
	}

	public void start() throws Exception {
		PerJobAppender.attachToRootLogger();

		this.server = new Server(PORT);

		ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("/");
		server.setHandler(context);

		context.addEventListener(new GuiceServletConfig());

		// Must add DefaultServlet for embedded Jetty
		// Failing to do this will cause 404 errors.
		context.addServlet(DefaultServlet.class, "/");

		FilterHolder filterHolder = new FilterHolder(GuiceFilter.class);
		context.addFilter(filterHolder, "*", EnumSet.of(DispatcherType.REQUEST));

		context.setClassLoader(Thread.currentThread().getContextClassLoader());

		server.start();
	}

	public void stop() throws Exception {
		if (server != null) {
			server.stop();
		}
	}

}
