package org.platformlayer.jdbc.simplejpa;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class JoinedQueryResult {
	public static class ObjectList<T> {
		public final Map<Object, T> objects = Maps.newHashMap();

		public boolean containsKey(Object key) {
			return objects.containsKey(key);
		}

		public void put(Object key, T newObject) {
			objects.put(key, newObject);
		}
	}

	public final Map<Class<?>, ObjectList<?>> types = Maps.newHashMap();

	private <T> ObjectList<T> getList(Class<T> clazz) {
		ObjectList<T> list = (ObjectList<T>) types.get(clazz);
		return list;
	}

	public <T> Collection<T> getAll(Class<T> clazz) {
		ObjectList<T> list = getList(clazz);
		if (list == null) {
			return Collections.emptyList();
		}
		return list.objects.values();
	}

	public <T> T get(Class<T> clazz, Object key) {
		ObjectList<T> list = getList(clazz);
		if (list == null) {
			return null;
		}
		return list.objects.get(key);
	}

	public <T> T getOneOrNull(Class<T> clazz) {
		ObjectList<T> list = getList(clazz);
		if (list == null) {
			return null;
		}
		if (list.objects.size() == 0) {
			return null;
		}
		return Iterables.getOnlyElement(list.objects.values());
	}
}
