package org.platformlayer.ops.helpers;

import java.util.List;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.InstanceBase;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.ops.OpsException;

import com.google.common.collect.Lists;

public class InstanceFinder extends TreeWalker {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(InstanceFinder.class);

	List<InstanceBase> instances = Lists.newArrayList();

	@Override
	protected void foundItem(ItemBase child) throws OpsException {
		super.foundItem(child);

		if (child instanceof InstanceBase) {
			instances.add((InstanceBase) child);
		}

		PlatformLayerKey assignedTo = Tag.ASSIGNED_TO.findUnique(child);
		if (assignedTo != null) {
			scheduleVisit(assignedTo);
		}
	}

	public List<InstanceBase> getInstances() {
		return instances;
	}

}
