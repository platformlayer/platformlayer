package org.platformlayer.ops.helpers;

import java.util.List;

import javax.inject.Inject;

import org.platformlayer.Filter;
import org.platformlayer.StateFilter;
import org.platformlayer.TagFilter;
import org.platformlayer.core.model.ManagedItemState;
import org.platformlayer.core.model.Tag;
import org.platformlayer.instances.model.PersistentInstance;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class InstanceSupervisor {
	static final Logger log = LoggerFactory.getLogger(InstanceSupervisor.class);

	@Inject
	PlatformLayerHelpers platformLayer;

	public PersistentInstance findPersistentInstance(Tag tag) throws OpsException {
		Filter filter = TagFilter.byTag(tag);
		filter = Filter.and(filter, StateFilter.exclude(ManagedItemState.DELETED));

		List<PersistentInstance> instances = Lists.newArrayList(platformLayer.listItems(PersistentInstance.class,
				filter));
		if (instances.size() == 0) {
			return null;
		}
		if (instances.size() != 1) {
			throw new OpsException("Found multiple instances with tag: " + tag);
		}
		return instances.get(0);
	}
}
