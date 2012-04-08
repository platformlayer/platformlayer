package org.platformlayer.service.platformlayer.ops;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.ManagedDirectory;
import org.platformlayer.ops.filesystem.ManagedSymlink;
import org.platformlayer.ops.java.JavaVirtualMachine;
import org.platformlayer.ops.maven.MavenReference;
import org.platformlayer.ops.metrics.collectd.OpsTreeBase;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.ops.supervisor.SupervisorInstance;
import org.platformlayer.ops.supervisor.SupervisordService;

public abstract class ZippedService extends OpsTreeBase {
    static final Logger log = Logger.getLogger(ZippedService.class);

    @Inject
    SoftwareRepositoryHelpers softwareRepository;

    @Handler
    public void doOperation() throws OpsException, IOException {
    }

    public File getLogDir() {
        return null;
    }

    public File getWorkDir() {
        return new File("/var", getFriendlyKey());
    }

    public String getFriendlyKey() {
        return getMavenReference().artifactId;
    }

    @Override
    protected void addChildren() throws OpsException {
        addChild(PackageDependency.build("unzip"));

        addChild(injected(SupervisordService.class));

        addChild(JavaVirtualMachine.buildJava7());

        {
            File logPath = getLogDir();
            if (logPath != null) {
                ManagedDirectory logDir = ManagedDirectory.build(logPath, "755");
                addChild(logDir);
            }
        }

        MavenReference mavenReference = getMavenReference();

        File rootDir = new File("/opt/" + getFriendlyKey() + "/current");
        File workDir = getWorkDir();

        {
            MavenFile zip = injected(MavenFile.class);

            zip.mavenReference = mavenReference;

            zip.basePath = softwareRepository.getMavenBasePath();

            zip.repositoryPath = new File("/var/repository");
            zip.expandPath = rootDir;

            addChild(zip);
        }

        String supervisorKey = getFriendlyKey();

        {
            ManagedSymlink symlink = ManagedSymlink.build(new File("/etc/supervisor/conf.d/" + supervisorKey + ".conf"), new File(workDir, "supervisord.conf"));
            addChild(symlink);
        }

        {
            SupervisorInstance service = injected(SupervisorInstance.class);
            service.id = supervisorKey;
            addChild(service);
        }
    }

    protected abstract MavenReference getMavenReference();
}
