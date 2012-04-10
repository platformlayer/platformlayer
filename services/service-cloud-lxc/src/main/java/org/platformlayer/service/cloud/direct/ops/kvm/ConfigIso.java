package org.platformlayer.service.cloud.direct.ops.kvm;

import java.io.File;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.ManagedDirectory;
import org.platformlayer.ops.filesystem.TemplatedFile;
import org.platformlayer.ops.templates.TemplateDataSource;
import org.platformlayer.ops.tree.OpsTreeBase;

public class ConfigIso extends OpsTreeBase {
    public File isoFile;
    public File buildDir;
    public TemplateDataSource model;

    @Handler
    public void handler() {
    }

    private File getBuildDir() {
        return buildDir;
    }

    @Override
    protected void addChildren() throws OpsException {
        addChild(ManagedDirectory.build(new File(getBuildDir(), "root/.ssh"), "700"));
        addChild(TemplatedFile.build(model, new File(getBuildDir(), "root/.ssh/authorized_keys")));
        addChild(ManagedDirectory.build(new File(getBuildDir(), "etc/network"), "755"));
        addChild(TemplatedFile.build(model, new File(getBuildDir(), "etc/network/interfaces")));

        {
            MkIsoFs mkiso = injected(MkIsoFs.class);
            mkiso.srcDir = getBuildDir();
            mkiso.iso = isoFile;
            mkiso.volumeLabel = "cloud_config";
            addChild(mkiso);
        }
    }
}
