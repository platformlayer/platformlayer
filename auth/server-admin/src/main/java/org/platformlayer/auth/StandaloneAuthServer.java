package org.platformlayer.auth;

import java.io.File;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.webapp.WebAppContext;

public class StandaloneAuthServer {
    static final int PORT = 8081;

    private Server server;

    public static void main(String[] args) throws Exception {
        System.setProperty("application.mode", "development");

        StandaloneAuthServer server = new StandaloneAuthServer();
        server.start();

        // try {
        // while (true) {
        // Thread.sleep(5000);
        // }
        // } finally {
        // server.stop();
        // }
    }

    public void start() throws Exception {
        this.server = new Server(PORT);
        ContextHandlerCollection contextHandlerCollection = new ContextHandlerCollection();

        WebAppContext root = new WebAppContext();

        File base = new File(".").getCanonicalFile();
        root.setWar(new File(base, "src/main/webapp").getCanonicalPath());
        root.setContextPath("/");
        contextHandlerCollection.addHandler(root);

        server.setHandler(contextHandlerCollection);

        server.start();

    }

    public void stop() throws Exception {
        if (server != null)
            server.stop();
    }

}
