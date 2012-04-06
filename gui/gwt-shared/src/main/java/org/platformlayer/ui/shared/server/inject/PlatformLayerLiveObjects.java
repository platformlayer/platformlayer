package org.platformlayer.ui.shared.server.inject;

import java.util.Map;

import org.platformlayer.Scope;

import com.google.common.collect.Maps;

public class PlatformLayerLiveObjects {
    class ClassInstances<T> {
        final Map<String, T> instances = Maps.newHashMap();

        public T findInstance(String key) {
            return instances.get(key);
        }
    }

    final Map<Class<?>, ClassInstances<?>> byClass = Maps.newHashMap();

    <T> ClassInstances<T> getClassInstances(Class<T> clazz) {
        ClassInstances<T> classInstances = (ClassInstances<T>) byClass.get(clazz);
        if (classInstances == null) {
            classInstances = new ClassInstances<T>();
            byClass.put(clazz, classInstances);
        }
        return classInstances;
    }

    <T> T findInContext(Class<T> clazz, String id) {
        return getClassInstances(clazz).findInstance(id);
    }

    public static PlatformLayerLiveObjects get() {
        Scope scope = Scope.get();
        PlatformLayerLiveObjects o = scope.get(PlatformLayerLiveObjects.class);
        if (o == null) {
            o = new PlatformLayerLiveObjects();
            scope.put(o);
        }
        return o;
    }

    public <T> void notifyCreated(T o, String id) {
        ClassInstances<T> classInstances = getClassInstances((Class<T>) o.getClass());
        classInstances.instances.put(id, o);
    }

    public <T> void notifyLoaded(T o, String id) {
        ClassInstances<T> classInstances = getClassInstances((Class<T>) o.getClass());
        classInstances.instances.put(id, o);
    }
}
