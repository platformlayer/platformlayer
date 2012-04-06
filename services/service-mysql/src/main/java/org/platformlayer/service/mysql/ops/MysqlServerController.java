package org.platformlayer.service.mysql.ops;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.Secret;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.filesystem.SimpleFile;
import org.platformlayer.ops.instances.DiskImageRecipeBuilder;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.metrics.collectd.ManagedService;
import org.platformlayer.ops.metrics.collectd.OpsTreeBase;
import org.platformlayer.ops.networks.PublicEndpoint;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.service.mysql.model.MysqlServer;

public class MysqlServerController extends OpsTreeBase {
    public static final Secret DEFAULT_BOOTSTRAP_PASSWORD = Secret.build("bootstrap_password");
    static final Logger log = Logger.getLogger(MysqlServerController.class);

    @Handler
    public void handler() throws OpsException, IOException {
    }

    @Override
    protected void addChildren() throws OpsException {
        MysqlServer model = OpsContext.get().getInstance(MysqlServer.class);

        InstanceBuilder instance = InstanceBuilder.build(model.dnsName, DiskImageRecipeBuilder.buildDiskImageRecipe(this));
        // TODO: Memory _really_ needs to be configurable here!

        instance.publicPorts.add(3306);

        instance.minimumMemoryMb = 2048;

        instance.hostPolicy.allowRunInContainer = true;
        addChild(instance);

        {
            PackageDependency serverPackage = instance.addChild(PackageDependency.build("mysql-server"));
            // mysql-server-5.1 mysql-server/root_password_again password
            // mysql-server-5.1 mysql-server/root_password password
            // mysql-server-5.1 mysql-server-5.1/start_on_boot boolean true
            // mysql-server-5.1 mysql-server-5.1/postrm_remove_databases boolean false
            // mysql-server-5.1 mysql-server/error_setting_password error
            // mysql-server-5.1 mysql-server-5.1/nis_warning note
            // mysql-server-5.1 mysql-server-5.1/really_downgrade boolean false
            // mysql-server-5.1 mysql-server/password_mismatch error
            // mysql-server-5.1 mysql-server/no_upgrade_when_using_ndb error

            // We need to install with a default password, which we then change
            String plaintextPassword = DEFAULT_BOOTSTRAP_PASSWORD.plaintext();
            serverPackage.addConfiguration("mysql-server-5.1", "mysql-server/root_password", "password", plaintextPassword);
            serverPackage.addConfiguration("mysql-server-5.1", "mysql-server/root_password_again", "password", plaintextPassword);
        }

        // TODO: Is there a window of vulnerability when first booting a machine?
        // Do we need to secure it so that mysql doesn't listen remotely initially (or isn't running)?
        // Maybe use mysql-server-5.1 mysql-server-5.1/start_on_boot boolean true

        instance.addChild(PackageDependency.build("mysql-client"));

        instance.addChild(MysqlServerBootstrap.build());

        instance.addChild(SimpleFile.build(getClass(), new File("/etc/mysql/conf.d/bind_all.cnf")));
        instance.addChild(SimpleFile.build(getClass(), new File("/etc/mysql/conf.d/skip_name_resolve.cnf")));

        // Collectd not restarting correctly (doesn't appear to be hostname problems??)
        // instance.addChild(CollectdCollector.build());

        {
            PublicEndpoint endpoint = injected(PublicEndpoint.class);
            // endpoint.network = null;
            endpoint.publicPort = 3306;
            endpoint.backendPort = 3306;
            endpoint.dnsName = model.dnsName;

            endpoint.tagItem = OpsSystem.toKey(model);
            endpoint.parentItem = OpsSystem.toKey(model);

            instance.addChild(endpoint);
        }

        instance.addChild(ManagedService.build("mysql"));
    }
}
