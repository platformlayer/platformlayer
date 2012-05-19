package org.platformlayer.ops.helpers;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.UntypedItem;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.ManagedItemState;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.machines.PlatformLayerHelpers;

public class TreeWalker {
	private static final Logger log = Logger.getLogger(TreeWalker.class);

	@Inject
	PlatformLayerHelpers platformLayer;

	public void visitChildren(PlatformLayerKey parentKey) throws OpsException, OpsException {
		for (UntypedItem child : platformLayer.listChildren(parentKey)) {
			if (child.getState() == ManagedItemState.DELETED) {
				// TODO: Push up into listChildren??
				log.warn("Skipping deleted item: " + child);
				continue;
			}

			// log.debug("Child = " + child.serialize());

			ItemBase typedChild = platformLayer.promoteToTyped(child);
			foundChild(typedChild);
		}
	}

	public void foundChild(ItemBase child) throws OpsException {
		visitChildren(child.getKey());
	}

}
