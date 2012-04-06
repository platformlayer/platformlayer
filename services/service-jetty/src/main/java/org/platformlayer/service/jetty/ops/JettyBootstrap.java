package org.platformlayer.service.jetty.ops;

import java.io.File;
import java.io.IOException;

import org.platformlayer.ResourceUtils;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;

public class JettyBootstrap {
    @Handler
    public void handler() throws OpsException, IOException {
        // TODO: This needs to be idempotent

        OpsTarget target = OpsContext.get().getInstance(OpsTarget.class);

        File webappsDir = new File("/var/lib/jetty/webapps");
        target.mv(webappsDir, new File("/var/lib/jetty/webapps.default"));
        target.mkdir(webappsDir, "755");
        target.chown(webappsDir, "jetty", "adm", false, false);

        File warsDir = new File("/var/lib/jetty/wars");
        target.mkdir(warsDir, "755");
        target.chown(warsDir, "jetty", "adm", false, false);

        File contextsDir = new File("/etc/jetty/contexts");
        target.mv(contextsDir, new File("/etc/jetty/contexts.default"));
        target.mkdir(contextsDir, "755");
        target.chown(contextsDir, "root", "root", false, false);

        // We've set NO_START=0 in /etc/default/jetty, also listen host to 0.0.0.0
        target.setFileContents(new File("/etc/default/jetty"), ResourceUtils.get(getClass(), "etc.default.jetty"));
    }

    public static JettyBootstrap build() {
        return Injection.getInstance(JettyBootstrap.class);
    }
}
