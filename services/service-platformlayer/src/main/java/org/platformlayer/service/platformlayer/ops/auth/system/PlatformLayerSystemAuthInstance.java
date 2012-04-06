package org.platformlayer.service.platformlayer.ops.auth.system;

import java.io.File;

import org.apache.log4j.Logger;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.filesystem.ManagedDirectory;
import org.platformlayer.ops.filesystem.TemplatedFile;
import org.platformlayer.ops.maven.MavenReference;
import org.platformlayer.ops.networks.PublicEndpoint;
import org.platformlayer.service.platformlayer.model.PlatformLayerService;
import org.platformlayer.service.platformlayer.ops.ZippedService;

public class PlatformLayerSystemAuthInstance extends ZippedService {
    public static final int PORT_AUTH_SYSTEM = 35357;

    static final Logger log = Logger.getLogger(PlatformLayerSystemAuthInstance.class);

    @Override
    protected void addChildren() throws OpsException {
        File workDir = getWorkDir();

        addChild(ManagedDirectory.build(workDir, "755"));

        TemplateData templateData = injected(TemplateData.class);
        {
            TemplatedFile conf = TemplatedFile.build(templateData, new File(workDir, "configuration.properties"));
            addChild(conf);
        }

        {
            TemplatedFile conf = TemplatedFile.build(templateData, new File(workDir, "supervisord.conf"));
            addChild(conf);
        }

        super.addChildren();

        PlatformLayerService model = OpsContext.get().getInstance(PlatformLayerService.class);

        {
            PublicEndpoint endpoint = injected(PublicEndpoint.class);
            // endpoint.network = null;
            endpoint.publicPort = PORT_AUTH_SYSTEM;
            endpoint.backendPort = PORT_AUTH_SYSTEM;
            endpoint.dnsName = model.dnsName;

            endpoint.tagItem = OpsSystem.toKey(model);
            endpoint.parentItem = OpsSystem.toKey(model);

            addChild(endpoint);
        }
    }

    @Override
    public File getLogDir() {
        return new File("/var/log/platformlayer");
    }

    @Override
    public String getFriendlyKey() {
        return "platformlayer-auth-system";
    }

    @Override
    protected MavenReference getMavenReference() {
        MavenReference mavenReference = new MavenReference();
        mavenReference.groupId = "org.platformlayer";
        mavenReference.artifactId = "package-platformlayer-auth-system";
        mavenReference.classifier = "package";
        return mavenReference;
    }
}
