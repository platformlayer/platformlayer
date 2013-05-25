package org.platformlayer.ops.tree;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.platformlayer.ops.BindingScope;
import org.platformlayer.ops.CustomRecursor;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OperationRecursor;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTree;
import org.platformlayer.ops.filesystem.ManagedDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.Casts;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public abstract class OpsTreeBase extends OpsItemBase implements OpsTree, CustomRecursor {
	static final Logger log = LoggerFactory.getLogger(OpsTreeBase.class);

	private List<Object> children = null;

	@Override
	public List<Object> getChildren() throws OpsException {
		if (children == null) {
			children = Lists.newArrayList();
			addChildren();
		}
		return children;
	}

	public <T> Iterable<T> getChildren(Class<T> filterByClass) throws OpsException {
		return Iterables.filter(getChildren(), filterByClass);
	}

	public <T> T addChild(T child) throws OpsException {
		getChildren().add(child);
		return child;
	}

	public <T> T addChild(Class<T> childClass) throws OpsException {
		T child = injected(childClass);
		getChildren().add(child);
		return child;
	}

	public void addChildren(Collection<?> children) throws OpsException {
		getChildren().addAll(children);
	}

	protected <T> T getChild(Class<T> findClass) throws OpsException {
		for (Object child : getChildren()) {
			if (findClass.isInstance(child)) {
				return Casts.as(child, findClass);
			}
		}
		throw new OpsException("Could not find child matching type: " + findClass);
	}

	public static <T> T injected(Class<T> clazz) {
		return OpsContext.injected(clazz);
	}

	protected abstract void addChildren() throws OpsException;

	RecursionState recursionState;

	protected RecursionState getRecursionState() {
		if (recursionState == null) {
			recursionState = new RecursionState();
		}
		return recursionState;
	}

	public static class RecursionState {
		Map<Class<?>, Object> childScope = Maps.newHashMap();
		boolean preventRecursion = false;

		public <T> T pushChildScope(Class<T> clazz, T o) {
			childScope.put(clazz, o);
			return o;
		}

		public <T> T pushChildScope(T o) {
			if (o == null) {
				throw new IllegalArgumentException();
			}
			Class<T> clazz = (Class<T>) o.getClass();
			return pushChildScope(clazz, o);
		}

		public boolean isPreventRecursion() {
			return preventRecursion;
		}

		public void setPreventRecursion(boolean preventRecursion) {
			this.preventRecursion = preventRecursion;
		}
	}

	@Override
	public void doRecurseOperation() throws OpsException {
		BindingScope scope = null;

		try {
			// TODO: Is this actually safe?
			RecursionState recursionState = this.recursionState;
			this.recursionState = null;

			if (recursionState != null) {
				if (recursionState.preventRecursion) {
					log.warn("Skipping recursion into child items");
					return;
				}
				if (!recursionState.childScope.isEmpty()) {
					scope = BindingScope.push(recursionState.childScope.values());
				}
			}

			OpsContext opsContext = OpsContext.get();
			OperationRecursor.doRecurseChildren(opsContext, this);
		} finally {
			if (scope != null) {
				scope.pop();
			}
		}
	}

	protected ManagedDirectory findDirectory(File dir) throws OpsException {
		for (ManagedDirectory o : getChildren(ManagedDirectory.class)) {
			if (o.filePath.equals(dir)) {
				return o;
			}
		}
		return null;
	}

	@Handler
	public void handler() throws Exception {
	}
}
