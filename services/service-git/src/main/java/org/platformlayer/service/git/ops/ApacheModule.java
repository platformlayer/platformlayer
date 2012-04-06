package org.platformlayer.service.git.ops;

import java.io.File;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.ManagedSymlink;
import org.platformlayer.ops.metrics.collectd.OpsTreeBase;

public class ApacheModule extends OpsTreeBase {
    String moduleName;

    @Handler
    public void handler() throws OpsException {
    }

    public static ApacheModule build(String moduleName) {
        ApacheModule apacheModule = Injection.getInstance(ApacheModule.class);
        apacheModule.moduleName = moduleName;
        return apacheModule;
    }

    @Override
    protected void addChildren() throws OpsException {
        File apache2ConfDir = new File("/etc/apache2");
        File modsAvailableDir = new File(apache2ConfDir, "mods-available");
        File modsEnabledDir = new File(apache2ConfDir, "mods-enabled");
        String moduleFile = moduleName + ".load";

        File symlinkAvailable = new File(modsAvailableDir, moduleFile);
        File symlinkEnabled = new File(modsEnabledDir, moduleFile);

        addChild(ManagedSymlink.build(symlinkEnabled, symlinkAvailable));
    }

}
