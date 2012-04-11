package org.openstack.keystone.server;

import java.io.File;
import java.util.EnumSet;

import org.eclipse.jetty.server.DispatcherType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.platformlayer.WellKnownPorts;

import com.google.inject.servlet.GuiceFilter;

public class KeystoneUserServer {
    private Server server;

    public static void main(String[] args) throws Exception {
        File base = new File(".").getCanonicalFile();

        KeystoneUserServer server = new KeystoneUserServer();
        server.start(base, WellKnownPorts.PORT_PLATFORMLAYER_AUTH_USER);
    }

    public void start(File base, int port) throws Exception {
        this.server = new Server(port);

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        server.setHandler(context);

        context.addEventListener(new UserServerConfig());

        // Must add DefaultServlet for embedded Jetty
        // Failing to do this will cause 404 errors.
        context.addServlet(DefaultServlet.class, "/");

        FilterHolder filterHolder = new FilterHolder(GuiceFilter.class);
        context.addFilter(filterHolder, "*", EnumSet.of(DispatcherType.REQUEST));

        context.setClassLoader(Thread.currentThread().getContextClassLoader());

        server.start();
    }

    public void stop() throws Exception {
        if (server != null)
            server.stop();
    }
}
