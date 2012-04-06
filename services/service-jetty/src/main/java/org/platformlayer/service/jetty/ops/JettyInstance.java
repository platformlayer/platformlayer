package org.platformlayer.service.jetty.ops;

import java.io.IOException;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.java.JavaVirtualMachine;
import org.platformlayer.ops.metrics.collectd.ManagedService;
import org.platformlayer.ops.metrics.collectd.OpsTreeBase;
import org.platformlayer.ops.packages.PackageDependency;

public class JettyInstance extends OpsTreeBase {
    @Handler
    public void doOperation() throws OpsException, IOException {
    }

    @Override
    protected void addChildren() throws OpsException {
        addChild(JavaVirtualMachine.buildJava6());
        addChild(PackageDependency.build("jetty"));

        addChild(JettyBootstrap.build());
        addChild(AppsContainer.build());

        addChild(ManagedService.build("jetty"));
    }

    public static JettyInstance build() {
        return Injection.getInstance(JettyInstance.class);
    }

    public void addApp(Object app) throws OpsException {
        getChild(AppsContainer.class).addChild(app);
    }

}
