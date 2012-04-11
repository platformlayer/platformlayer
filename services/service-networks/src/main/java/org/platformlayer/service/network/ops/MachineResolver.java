package org.platformlayer.service.network.ops;

import javax.inject.Inject;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.BindingScope;
import org.platformlayer.ops.CustomRecursor;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OperationRecursor;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.tree.OpsTreeBase;

public class MachineResolver extends OpsTreeBase implements CustomRecursor {

    public PlatformLayerKey key;

    @Inject
    PlatformLayerHelpers platformLayerHelpers;

    @Inject
    InstanceHelpers instanceHelpers;

    @Override
    protected void addChildren() throws OpsException {

    }

    @Handler
    public void handler() {
    }

    public static MachineResolver build(PlatformLayerKey key) {
        if (key == null) {
            throw new IllegalArgumentException();
        }
        MachineResolver resolver = injected(MachineResolver.class);
        resolver.key = key;
        return resolver;
    }

    @Override
    public void doRecurseOperation() throws OpsException {
        ItemBase dest = platformLayerHelpers.getItem(key);

        boolean required = !OpsContext.isDelete();
        for (Machine machine : instanceHelpers.getMachines(dest, required)) {
            OpsTarget target = instanceHelpers.getTarget(dest, machine);

            BindingScope scope = BindingScope.push(machine, target);
            try {
                OpsContext opsContext = OpsContext.get();
                OperationRecursor.doRecurseChildren(opsContext, this);
            } finally {
                scope.pop();
            }
        }
    }

}
