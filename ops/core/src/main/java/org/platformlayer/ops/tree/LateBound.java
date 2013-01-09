package org.platformlayer.ops.tree;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.HasDescription;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LateBound<T> extends OpsTreeBase implements HasDescription {

	private static final Logger log = LoggerFactory.getLogger(LateBound.class);

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

	public static <T> LateBound<T> of(final Class<T> clazz, final String description) {
		return new LateBound<T>() {
			@Override
			protected T get() throws OpsException {
				return Injection.getInstance(clazz);
			}

			@Override
			public String getDescription() throws Exception {
				return description;
			}
		};
	}
}
