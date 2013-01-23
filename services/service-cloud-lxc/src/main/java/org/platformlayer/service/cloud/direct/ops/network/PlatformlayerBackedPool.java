package org.platformlayer.service.cloud.direct.ops.network;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.platformlayer.TimeSpan;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.TagChanges;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.pool.PoolBuilder;
import org.platformlayer.ops.pool.ResourcePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class PlatformlayerBackedPool<T> implements ResourcePool<T> {
	// private static final String KEY = "assigned:";

	private static final Logger log = LoggerFactory.getLogger(PlatformlayerBackedPool.class);

	final PlatformLayerHelpers platformLayer;
	final PlatformLayerKey resourceKey;
	// final Class<T> childType;

	final PoolBuilder<T> adapter;

	public PlatformlayerBackedPool(PlatformLayerHelpers platformLayer, PlatformLayerKey resourceKey,
			Class<T> childType, PoolBuilder<T> adapter) {
		super();
		this.platformLayer = platformLayer;
		this.resourceKey = resourceKey;
		// this.childType = childType;
		this.adapter = adapter;
	}

	// T pickUnassigned() throws OpsException {
	// for (int i = 0; i < 2; i++) {
	// List<T> items = Lists.newArrayList();
	//
	// for (T item : platformLayer.listItems(childType, TagFilter.byParent(parentKey))) {
	// items.add(item);
	// }
	//
	// Collections.shuffle(items);
	//
	// T found = null;
	// for (T item : items) {
	// if (Tag.ASSIGNED_TO.findFirst(item) == null) {
	// found = item;
	// break;
	// }
	// }
	//
	// if (found != null) {
	// return found;
	// }
	//
	// // TODO: We should implement resource reclamation by checking that symlink targets exist.
	// // (We should probably avoid doing this on too many threads concurrently)
	// if (i == 0) {
	// extendPool();
	// }
	// }
	//
	// return null;
	// }

	String pickUnassigned(ItemBase item) throws OpsException {
		Map<String, String> assignments = findAssignments(item);

		// for (T item : platformLayer.listItems(childType, TagFilter.byParent(resourceKey))) {
		// items.add(item);
		// }

		Iterator<String> keys = adapter.getItems().iterator();
		String found = null;
		while (found == null) {
			// We shuffle them in batches (it's a potentially infinite list, so this is a compromise)
			List<String> batch = Lists.newArrayList();
			while (keys.hasNext() && batch.size() < 100) {
				batch.add(keys.next());
			}
			Collections.shuffle(batch);

			if (batch.isEmpty()) {
				break;
			}

			for (String key : batch) {
				if (!assignments.containsKey(key)) {
					found = key;
					break;
				}
			}
		}

		if (found != null) {
			return found;
		}

		// // TODO: We should implement resource reclamation by checking that symlink targets exist.
		// // (We should probably avoid doing this on too many threads concurrently)
		// if (i == 0) {
		// extendPool();
		// }

		return null;
	}

	private Map<String, String> findAssignments(ItemBase item) {
		Map<String, String> assignments = Maps.newHashMap();

		for (String value : Tag.POOL_ASSIGNMENT.find(item)) {
			// We normalize the keys, so we don't want to create a key-per-resource
			// Instead we store it in the value

			int equalsPos = value.indexOf('=');
			if (equalsPos == -1) {
				log.warn("Corrupted POOL_ASSIGNMENT key: " + value);
				continue;
			}

			String assignedKey = value.substring(0, equalsPos);
			String target = value.substring(equalsPos + 1);

			assignments.put(assignedKey, target);
		}

		return assignments;
	}

	@Override
	public T assign(PlatformLayerKey owner, boolean required) throws OpsException {
		T assigned = findAssigned(owner);
		if (assigned != null) {
			return assigned;
		}

		for (int i = 0; i < 10; i++) {
			ItemBase item = platformLayer.getItem(resourceKey);

			String unassigned = pickUnassigned(item);
			if (unassigned == null) {
				break;
			}

			Tag assignmentTag = Tag.POOL_ASSIGNMENT.build(unassigned + "=" + owner.getUrl());

			TagChanges tagChanges = new TagChanges();
			tagChanges.addTags.add(assignmentTag);
			if (null != platformLayer.changeTags(resourceKey, tagChanges, item.getVersion())) {
				return adapter.toItem(unassigned);
			}

			if (!TimeSpan.ONE_SECOND.doSafeSleep()) {
				break;
			}
		}

		if (required) {
			throw new OpsException("Unable to assign value from pool: " + toString());
		}
		return null;
	}

	@Override
	public T findAssigned(PlatformLayerKey holder) throws OpsException {
		// Tag assignedTag = Tag.ASSIGNED_TO.build(holder);
		//
		// Filter filter = Filter.and(TagFilter.byParent(resourceKey), TagFilter.byTag(assignedTag));
		//
		// List<T> assigned = platformLayer.listItems(childType, filter);
		// if (assigned == null || assigned.isEmpty()) {
		// return null;
		// }

		ItemBase item = platformLayer.getItem(resourceKey);
		Map<String, String> assignments = findAssignments(item);

		String find = holder.getUrl();
		List<String> keys = Lists.newArrayList();

		for (Map.Entry<String, String> assignment : assignments.entrySet()) {
			if (find.equals(assignment.getValue())) {
				keys.add(assignment.getKey());
			}
		}

		if (keys.size() == 0) {
			return null;
		}
		if (keys.size() == 1) {
			return adapter.toItem(keys.get(0));
		}

		throw new OpsException("Found multiple assignments to: " + holder);
	}

	@Override
	public void release(PlatformLayerKey owner, T item) throws OpsException {
		for (int i = 0; i < 10; i++) {
			ItemBase resource = platformLayer.getItem(resourceKey);

			Map<String, String> assignments = findAssignments(resource);

			String key = adapter.toKey(item);
			String assigned = assignments.get(key);
			if (assigned == null) {
				throw new OpsException("Resource not assigned");
			}

			if (!assigned.equals(owner.getUrl())) {
				throw new OpsException("Resource not held");
			}

			Tag assignmentTag = Tag.POOL_ASSIGNMENT.build(key + "=" + owner.getUrl());

			TagChanges tagChanges = new TagChanges();
			tagChanges.removeTags.add(assignmentTag);
			if (null != platformLayer.changeTags(resourceKey, tagChanges, resource.getVersion())) {
				return;
			}

			if (!TimeSpan.ONE_SECOND.doSafeSleep()) {
				break;
			}
		}

		// List<PlatformLayerKey> assignedTo = Tag.POOL_ASSIGNMENT.build(t) Tag.ASSIGNED_TO.find(item);
		// if (!assignedTo.contains(holder)) {
		// throw new OpsException("Resource not owned");
		// }
		//
		// platformLayer.deleteItem(item.getKey());
	}
	// protected void extendPool() throws OpsException {
	// // ensureCreated();
	//
	// int batchAddCount = 16;
	//
	// Set<String> resourceKeys = Sets.newHashSet();
	//
	// for (T item : platformLayer.listItems(childType, TagFilter.byParent(resourceKey))) {
	// resourceKeys.add(adapter.toKey(item));
	// }
	//
	// int added = 0;
	//
	// for (String key : adapter.getItems()) {
	// // String key = adapter.toKey(item);
	//
	// if (resourceKeys.contains(key)) {
	// continue;
	// }
	//
	// // Properties properties = buildProperties(item);
	//
	// if (!addResource(key)) {
	// // Presumably already exists
	// log.warn("Unexpectedly did not add resource: " + key);
	// continue;
	// }
	//
	// added++;
	//
	// if (added >= batchAddCount) {
	// break;
	// }
	// }
	//
	// if (added != 0) {
	// log.info("Added " + added + " items to pool");
	// } else {
	// log.warn("Adapter did not add any items to pool");
	// }
	// }
	//
	// private boolean addResource(String key) throws OpsException {
	// T item = adapter.toItem(key);
	//
	// Tag parentTag = Tag.PARENT.build(resourceKey);
	// item.getTags().add(parentTag);
	//
	// // String key = adapter.toKey(item);
	// Tag uniqueTag = UniqueTag.build(resourceKey, key);
	//
	// platformLayer.putItemByTag(item, uniqueTag);
	//
	// return true;
	// }

}
