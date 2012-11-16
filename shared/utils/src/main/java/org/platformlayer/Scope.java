package org.platformlayer;

import java.util.Map;

import com.fathomdb.Casts;
import com.google.common.collect.Maps;

public class Scope {
	static final ThreadLocal<Scope> threadLocalScopeMap = new ThreadLocal<Scope>();

	final Scope inherit;

	private Scope(Scope inherit) {
		this.inherit = inherit;
	}

	public static Scope get() {
		return threadLocalScopeMap.get();
	}

	Scope previous = null;
	boolean active = false;

	public void push() {
		if (active) {
			throw new IllegalStateException();
		}
		previous = threadLocalScopeMap.get();
		threadLocalScopeMap.set(this);
		active = true;
	}

	public void pop() {
		if (!active) {
			throw new IllegalStateException();
		}
		threadLocalScopeMap.set(previous);
		previous = null;
		active = false;
	}

	final Map<Class<?>, Object> items = Maps.newHashMap();

	final Object NULL_MARKER = new Object();

	public <T> T get(Class<T> itemClass) {
		Object o = items.get(itemClass);
		if (o == NULL_MARKER) {
			return null;
		}

		if (o != null) {
			return Casts.checkedCast(o, itemClass);
		}

		if (inherit != null) {
			return inherit.get(itemClass);
		}

		return null;
	}

	public void put(Object o) {
		items.put(o.getClass(), o);
	}

	public <T> void put(Class<T> itemClass, T item) {
		if (item == null) {
			items.put(itemClass, NULL_MARKER);
		} else {
			items.put(itemClass, item);
		}
	}

	public static Scope inherit() {
		Scope parent = Scope.get();
		Scope scope = new Scope(parent);
		return scope;
	}

	public static Scope empty() {
		return new Scope(null);
	}
}
