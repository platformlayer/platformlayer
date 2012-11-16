package org.platformlayer.ops;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.platformlayer.SimpleThreadLocal;

import com.fathomdb.Casts;
import com.google.common.collect.Lists;

public class BindingScope implements Closeable {
	final BindingScope parent;
	final ArrayList<Object> scopeObjects;

	public BindingScope(BindingScope parent, Iterable<Object> scopeObjects) {
		super();
		this.parent = parent;
		this.scopeObjects = scopeObjects != null ? Lists.newArrayList(scopeObjects) : Lists.newArrayList();
	}

	static final SimpleThreadLocal<BindingScopeStack> threadLocalMap = new SimpleThreadLocal<BindingScopeStack>() {
		@Override
		protected BindingScopeStack buildNewItem() {
			return new BindingScopeStack();
		}
	};

	public static BindingScope get() {
		BindingScopeStack context = threadLocalMap.getCurrent();
		return context.top();
	}

	static class BindingScopeStack {
		final List<BindingScope> stack = Lists.newArrayList();

		public BindingScope top() {
			if (stack.size() == 0) {
				return null;
			}
			return stack.get(stack.size() - 1);
		}

		public void push(BindingScope scope) {
			stack.add(scope);
		}

		public void pop(BindingScope scope) {
			if (top() != scope) {
				throw new IllegalStateException();
			}

			stack.remove(stack.size() - 1);
		}
	}

	public <T> T getInstance(Class<T> clazz) {
		for (int i = scopeObjects.size() - 1; i >= 0; i--) {
			Object scopeObject = scopeObjects.get(i);
			T t = Casts.as(scopeObject, clazz);
			if (t != null) {
				return t;
			}
		}
		if (parent != null) {
			return parent.getInstance(clazz);
		}
		return null;
	}

	public static BindingScope push(Iterable<Object> bindings) {
		BindingScopeStack context = threadLocalMap.getCurrent();
		BindingScope parent = context.top();

		BindingScope child = new BindingScope(parent, bindings);
		context.push(child);

		return child;
	}

	public static BindingScope push(Object... bindings) {
		return push(Arrays.asList(bindings));
	}

	public void pop() {
		BindingScopeStack context = threadLocalMap.getCurrent();
		context.pop(this);
	}

	@Override
	public void close() throws IOException {
		pop();
	}

}
