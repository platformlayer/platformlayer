package org.platformlayer.service.cloud.direct.ops.network;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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

import com.fathomdb.TimeSpan;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class PlatformlayerBackedPool<T> implements ResourcePool<T> {
	// private static final String KEY = "assigned:";

	private static final Logger log = LoggerFactory.getLogger(PlatformlayerBackedPool.class);

	final String subkey = null;

	final PlatformLayerHelpers platformLayer;
	final PlatformLayerKey resourceKey;
	// final Class<T> childType;

	final PoolBuilder<T> adapter;

	static class Assignment {
		public final String owner;
		public final String item;
		public final String subkey;

		public Assignment(String owner, String item, String subkey) {
			super();
			this.owner = owner;
			this.item = item;
			this.subkey = subkey;
		}

		static Assignment parse(String value) {
			// We normalize the keys, so we don't want to create a key-per-resource
			// Instead we store it in the value
			int equalsPos = value.indexOf('=');
			if (equalsPos == -1) {
				throw new IllegalArgumentException("Corrupted POOL_ASSIGNMENT key: " + value);
			}

			String item = value.substring(0, equalsPos);
			String owner = value.substring(equalsPos + 1);

			String subkey = null;
			int tildaIndex = item.indexOf("~~");
			if (tildaIndex != -1) {
				subkey = item.substring(0, tildaIndex);
				item = item.substring(tildaIndex + 2);
			}

			return new Assignment(owner, item, subkey);
		}

		public static Assignment find(List<Assignment> candidates, String item, String subkey) {
			for (Assignment candidate : candidates) {
				if (!item.equals(candidate.item)) {
					continue;
				}
				if (!Objects.equal(subkey, candidate.subkey)) {
					continue;
				}
				return candidate;
			}
			return null;
		}

		public Tag asTag() {
			return Tag.POOL_ASSIGNMENT.build(encode());
		}

		public String encode() {
			String coded;
			if (subkey == null) {
				coded = item + "=" + owner;
			} else {
				coded = subkey + "~~" + item + "=" + owner;
			}
			return coded;
		}
	}

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
		List<Assignment> assignments = findAssignments(item);

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
				if (Assignment.find(assignments, key, subkey) == null) {
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

	private List<Assignment> findAssignments(ItemBase item) {
		List<Assignment> assignments = Lists.newArrayList();

		for (String value : Tag.POOL_ASSIGNMENT.find(item)) {
			Assignment assignment = Assignment.parse(value);
			if (!Objects.equal(assignment.subkey, subkey)) {
				continue;
			}

			assignments.add(assignment);
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
			ItemBase resource = platformLayer.getItem(resourceKey);

			String assignedItem = pickUnassigned(resource);
			if (assignedItem == null) {
				break;
			}

			Assignment assignment = new Assignment(owner.getUrl(), assignedItem, subkey);
			Tag assignmentTag = assignment.asTag();

			TagChanges tagChanges = new TagChanges();
			tagChanges.addTags.add(assignmentTag);
			if (null != platformLayer.changeTags(resourceKey, tagChanges, resource.getVersion())) {
				return adapter.toItem(assignedItem);
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
		List<Assignment> assignments = findAssignments(item);
		String findOwner = holder.getUrl();

		List<String> keys = Lists.newArrayList();
		for (Assignment assignment : assignments) {
			if (findOwner.equals(assignment.owner)) {
				keys.add(assignment.item);
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

			List<Assignment> assignments = findAssignments(resource);

			String key = adapter.toKey(item);
			Assignment assigned = Assignment.find(assignments, key, subkey);
			if (assigned == null) {
				throw new OpsException("Resource not assigned");
			}

			if (!assigned.item.equals(owner.getUrl())) {
				throw new OpsException("Resource not held");
			}

			Tag assignmentTag = assigned.asTag();

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
