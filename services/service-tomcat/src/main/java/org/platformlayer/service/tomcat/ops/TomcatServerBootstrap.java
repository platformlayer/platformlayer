package org.platformlayer.service.tomcat.ops;

import java.io.File;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;

public class TomcatServerBootstrap {
    @Handler
    public void doOperation(OpsTarget target) throws OpsException {
        File webappsDir = new File("/var/lib/tomcat6/webapps");
        target.mv(webappsDir, new File("/var/lib/tomcat6/webapps.default"));
        target.mkdir(webappsDir, "775");
        target.chown(webappsDir, "tomcat6", "tomcat6", false, false);

        // We remove the root context, but we leave the ones relating to the manager app
        File rootContext = new File("/etc/tomcat6/Catalina/localhost/ROOT.xml");
        target.rm(rootContext);

        // File contextsDir = new File("/etc/jetty/contexts");
        // target.mv(contextsDir, new File("/etc/jetty/contexts.default"));
        // target.mkdir(contextsDir, "755");
        // target.chown(contextsDir, "root", "root", false);
    }

    public static TomcatServerBootstrap build() {
        return Injection.getInstance(TomcatServerBootstrap.class);
    }
}
