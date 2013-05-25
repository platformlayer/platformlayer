package org.platformlayer.ops.tree;

import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class OpsItemBase implements OpsItem {
	static final Logger log = LoggerFactory.getLogger(OpsItemBase.class);

	boolean lazyDelete = false;

	@Override
	public boolean isLazyDelete() {
		return lazyDelete;
	}

	@Override
	public void setLazyDelete(boolean lazyDelete) {
		this.lazyDelete = lazyDelete;
	}

	public static void setAllChildrenLazyDelete(OpsTreeBase parent, boolean lazyDelete) throws OpsException {
		for (Object child : parent.getChildren()) {
			if (child instanceof OpsItem) {
				((OpsItem) child).setLazyDelete(lazyDelete);
			} else {
				log.warn("Cannot set lazy-delete on " + child.getClass());
			}
		}
	}
}
