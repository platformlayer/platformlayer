package org.platformlayer.service.wordpress.ops;

import javax.inject.Inject;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Secret;
import org.platformlayer.ops.BindingScope;
import org.platformlayer.ops.CustomRecursor;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OperationRecursor;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.metrics.collectd.OpsTreeBase;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.service.mysql.model.MysqlServer;
import org.platformlayer.service.mysql.ops.MysqlTarget;

public class MysqlConnection extends OpsTreeBase implements CustomRecursor {
    public PlatformLayerKey key;
    public String username;
    public Secret password;

    @Inject
    PlatformLayerHelpers platformLayerHelpers;

    @Inject
    InstanceHelpers instanceHelpers;

    public static MysqlConnection build(PlatformLayerKey key) {
        MysqlConnection mysql = injected(MysqlConnection.class);
        mysql.key = key;
        return mysql;
    }

    @Handler
    public void handler() {
    }

    @Override
    public void doRecurseOperation() throws OpsException {
        MysqlServer mysqlServer = platformLayerHelpers.getItem(key, MysqlServer.class);

        String username = this.username;
        if (username == null) {
            username = "root";
            password = mysqlServer.rootPassword;
        }

        Machine machine = instanceHelpers.getMachine(mysqlServer);

        String address = machine.getAddress(NetworkPoint.forTargetInContext(), 3306);
        MysqlTarget mysql = new MysqlTarget(address, username, password);

        BindingScope scope = BindingScope.push(mysql);
        try {
            OpsContext opsContext = OpsContext.get();
            OperationRecursor.doRecurseChildren(opsContext, this);
        } finally {
            scope.pop();
        }
    }

    @Override
    protected void addChildren() throws OpsException {

    }

}
