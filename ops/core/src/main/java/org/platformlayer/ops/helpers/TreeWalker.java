package org.platformlayer.ops.helpers;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.common.UntypedItem;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.ManagedItemState;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.machines.PlatformLayerHelpers;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class TreeWalker {
	private static final Logger log = Logger.getLogger(TreeWalker.class);

	@Inject
	PlatformLayerHelpers platformLayer;

	List<PlatformLayerKey> queue = Lists.newArrayList();
	Set<PlatformLayerKey> visited = Sets.newHashSet();

	protected void visitChildren(PlatformLayerKey parentKey) throws OpsException, OpsException {
		for (UntypedItem child : platformLayer.listChildren(parentKey).getItems()) {
			if (child.getState() == ManagedItemState.DELETED) {
				// TODO: Push up into listChildren??
				log.warn("Skipping deleted item: " + child);
				continue;
			}

			// log.debug("Child = " + child.serialize());

			ItemBase typedChild = platformLayer.promoteToTyped(child);
			foundItem(typedChild);
		}
	}

	protected void foundItem(ItemBase child) throws OpsException {
		PlatformLayerKey key = child.getKey();
		visited.add(key);
		visitChildren(key);
	}

	public void scheduleVisit(PlatformLayerKey key) {
		queue.add(key);
	}

	public void visit(ItemBase item) throws OpsException {
		foundItem(item);
		visitQueue();
	}

	public void visitQueue() throws OpsException {
		for (int i = 0; i < queue.size(); i++) {
			PlatformLayerKey key = queue.get(i);
			if (visited.contains(key)) {
				continue;
			}
			visitChildren(key);
		}
	}

}
