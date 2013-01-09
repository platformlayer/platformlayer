package org.platformlayer.service.openldap.ops;

import javax.inject.Inject;

import org.platformlayer.TimeSpan;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.ops.BindingScope;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OperationRecursor;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.helpers.SshKey;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecurseOverAll {

	private static final Logger log = LoggerFactory.getLogger(RecurseOverAll.class);

	@Inject
	InstanceHelpers instances;

	@Inject
	PlatformLayerHelpers platformLayer;

	public void doRecursion(Object controller, SshKey sshKey, Class<? extends ItemBase> machineItemClass)
			throws OpsException {
		boolean failed = false;

		for (ItemBase machineItem : platformLayer.listItems(machineItemClass)) {
			switch (machineItem.getState()) {
			case ACTIVE:
				break;

			case DELETED:
				log.warn("Ignoring deleted server: " + machineItem);
				continue;

			default:
				log.warn("Item not yet active: " + machineItem);
				failed = true;
				continue;
			}

			Machine machine = instances.findMachine(machineItem);
			if (machine == null) {
				log.warn("Server instance not found: " + machineItem);
				failed = true;
				continue;
			}

			OpsTarget target = machine.getTarget(sshKey);

			try {
				// Execute the children in a scope with the paired item and machine
				BindingScope scope = BindingScope.push(machine, target, machineItem);
				try {
					OpsContext opsContext = OpsContext.get();
					OperationRecursor.doRecurseChildren(opsContext, controller);
				} finally {
					scope.pop();
				}
			} catch (OpsException e) {
				failed = true;
				log.warn("Error updating machine: " + machine, e);
			}
		}

		if (failed) {
			throw new OpsException("Could not update all servers").setRetry(TimeSpan.ONE_MINUTE);
		}

	}
}
