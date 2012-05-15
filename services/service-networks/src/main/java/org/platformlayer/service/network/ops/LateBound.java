package org.platformlayer.service.network.ops;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.tree.OpsTreeBase;

public abstract class LateBound<T> extends OpsTreeBase {
	private static final Logger log = Logger.getLogger(LateBound.class);

	boolean addedChildren = false;

	@Handler
	public void handler() throws OpsException {
		if (!addedChildren) {
			T child = get();
			if (child != null) {
				addChild(child);
			}
			addedChildren = true;
		}
	}

	@Override
	protected void addChildren() throws OpsException {

	}

	protected abstract T get() throws OpsException;
}
