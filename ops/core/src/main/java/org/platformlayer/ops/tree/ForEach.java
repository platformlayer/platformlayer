package org.platformlayer.ops.tree;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.*;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.ManagedItemState;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.BindingScope;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OperationRecursor;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.helpers.SshKey;
import org.platformlayer.ops.machines.PlatformLayerHelpers;

import com.fathomdb.TimeSpan;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class ForEach {
	static final Logger log = LoggerFactory.getLogger(ForEach.class);

	@Inject
	InstanceHelpers instances;

	@Inject
	PlatformLayerHelpers platformLayer;

	public void doRecursion(Object controller, SshKey sshKey, Class<? extends ItemBase> machineItemClass,
			Class<? extends ItemBase> dataItemClass) throws OpsException {
		boolean failed = false;

		OpsContext ops = OpsContext.get();

		List<ItemBase> dataItems = Lists.newArrayList();
		ItemBase contextDataItem = ops.getInstance(dataItemClass);
		if (contextDataItem != null) {
			dataItems.add(contextDataItem);
		} else {
			for (ItemBase dataItem : platformLayer.listItems(dataItemClass)) {
				dataItems.add(dataItem);
			}
		}

		Object contextMachine = ops.getInstance(machineItemClass);
		if (contextMachine != null) {
			// We are presumably building the machine item
			PlatformLayerKey targetItemKey = ops.getJobRecord().getTargetItemKey();
			ItemBase machineItem = (ItemBase) contextMachine;

			if (!Objects.equal(targetItemKey, machineItem.getKey())) {
				throw new OpsException("Expected to find same model");
			}

			Machine machine = instances.findMachine(machineItem);
			if (machine == null) {
				log.warn("Server instance not found: " + contextMachine);
				failed = true;
			} else {
				OpsTarget target = machine.getTarget(sshKey);
				failed |= processDataItems(controller, dataItems, machineItem, machine, target);
			}
		} else {
			// We are building a data item
			for (ItemBase machineItem : platformLayer.listItems(machineItemClass)) {
				if (machineItem.getState() != ManagedItemState.ACTIVE) {
					log.warn("Machine not yet active: " + machineItem);
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

				failed |= processDataItems(controller, dataItems, machineItem, machine, target);
			}
		}

		if (failed) {
			throw new OpsException("Could not update all servers").setRetry(TimeSpan.ONE_MINUTE);
		}

	}

	private boolean processDataItems(Object controller, List<ItemBase> dataItems, ItemBase machineItem,
			Machine machine, OpsTarget target) {
		boolean failed = false;

		for (ItemBase dataItem : dataItems) {
			try {
				// Execute the children in a scope
				BindingScope scope = BindingScope.push(machine, target, machineItem, dataItem);
				try {
					OpsContext opsContext = OpsContext.get();
					OperationRecursor.doRecurseChildren(opsContext, controller);
				} finally {
					scope.pop();
				}
			} catch (OpsException e) {
				failed = true;
				log.warn("Error updating machine: " + machine + " with item " + dataItem, e);
			}
		}
		return failed;
	}
}
