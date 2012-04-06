package org.platformlayer.service.platformlayer.ops;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.instances.DiskImageRecipeBuilder;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.java.JavaVirtualMachine;
import org.platformlayer.ops.metrics.collectd.OpsTreeBase;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.ops.supervisor.SupervisordService;
import org.platformlayer.service.network.ops.PlatformLayerFirewallEntry;
import org.platformlayer.service.platformlayer.model.PlatformLayerService;
import org.platformlayer.service.platformlayer.ops.auth.system.PlatformLayerSystemAuthInstance;
import org.platformlayer.service.platformlayer.ops.auth.user.PlatformLayerUserAuthInstance;
import org.platformlayer.service.platformlayer.ops.backend.PlatformLayerInstance;

import com.google.common.collect.Lists;

public class PlatformLayerServiceController extends OpsTreeBase {
    static final Logger log = Logger.getLogger(PlatformLayerServiceController.class);

    @Inject
    SoftwareRepositoryHelpers softwareRepository;

    @Handler
    public void doOperation() throws OpsException, IOException {
    }

    @Override
    protected void addChildren() throws OpsException {
        PlatformLayerService model = OpsContext.get().getInstance(PlatformLayerService.class);

        List<Integer> ports = Lists.newArrayList();
        ports.add(PlatformLayerInstance.PORT_PLATFORMLAYER);
        ports.add(PlatformLayerSystemAuthInstance.PORT_AUTH_SYSTEM);
        ports.add(PlatformLayerUserAuthInstance.PORT_AUTH_USER);

        InstanceBuilder instance = InstanceBuilder.build(model.dnsName, DiskImageRecipeBuilder.buildDiskImageRecipe(this));
        instance.publicPorts.addAll(ports);

        instance.hostPolicy.allowRunInContainer = true;
        instance.minimumMemoryMb = 2048;
        addChild(instance);

        instance.addChild(PackageDependency.build("unzip"));
        instance.addChild(injected(SupervisordService.class));
        instance.addChild(JavaVirtualMachine.buildJava7());

        {
            PlatformLayerFirewallEntry net = injected(PlatformLayerFirewallEntry.class);

            net.destItem = model.database;
            net.port = PostgresTarget.POSTGRES_PORT;
            net.sourceItemKey = OpsSystem.toKey(model);

            instance.addChild(net);
        }

        instance.addChild(PackageDependency.build("postgresql-client"));

        PostgresqlConnection pgConnection = instance.addChild(PostgresqlConnection.build(model.database));
        instance.addChild(pgConnection);

        CommonTemplateData templateData = injected(CommonTemplateData.class);

        {
            PostgresDatabase db = injected(PostgresDatabase.class);
            db.databaseName = templateData.getDatabaseName();
            pgConnection.addChild(db);
        }

        {
            PostgresUser db = injected(PostgresUser.class);
            db.databaseName = templateData.getDatabaseName();
            db.databaseUser = templateData.getDatabaseUsername();
            db.databasePassword = templateData.getDatabasePassword();
            pgConnection.addChild(db);
        }

        instance.addChild(injected(PlatformLayerInstance.class));
        instance.addChild(injected(PlatformLayerSystemAuthInstance.class));
        instance.addChild(injected(PlatformLayerUserAuthInstance.class));

        {
            PlatformLayerFirewallEntry net = injected(PlatformLayerFirewallEntry.class);

            net.destItem = OpsSystem.toKey(model);
            net.sourceCidr = "127.0.0.1/32";
            net.port = PlatformLayerSystemAuthInstance.PORT_AUTH_SYSTEM;

            instance.addChild(net);
        }
    }
}
