package org.platformlayer.ops.dns;

import java.io.File;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.TemplatedFile;
import org.platformlayer.ops.tree.OpsTreeBase;

public class DnsResolver extends OpsTreeBase {
    @Handler
    public void doOperation() throws OpsException {
    }

    @Override
    protected void addChildren() throws OpsException {
        addChild(TemplatedFile.build(Injection.getInstance(DnsResolverModuleBuilder.class), new File("/etc/resolv.conf")).setFileMode("644"));
    }
}
