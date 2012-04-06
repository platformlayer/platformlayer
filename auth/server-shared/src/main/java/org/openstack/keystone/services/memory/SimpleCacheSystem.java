package org.openstack.keystone.services.memory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.openstack.keystone.services.CacheSystem;
import org.platformlayer.CastUtils;
import org.platformlayer.IoUtils;
import org.platformlayer.TimeSpan;

public class SimpleCacheSystem implements CacheSystem {
    static final Logger log = Logger.getLogger(SimpleCacheSystem.class);

    static class LruHashMap<K, V> {
        final LinkedHashMap<K, V> map;
        final int sizeLimit;

        public LruHashMap(int sizeLimit) {
            this.sizeLimit = sizeLimit;

            boolean accessOrder = true;
            float loadFactor = 0.75f;
            int initialCapacity = 16 + (int) (sizeLimit / loadFactor);
            this.map = new LinkedHashMap<K, V>(initialCapacity, loadFactor, accessOrder) {
                private static final long serialVersionUID = 1L;

                @Override
                protected boolean removeEldestEntry(Entry<K, V> eldest) {
                    return size() > LruHashMap.this.sizeLimit;
                }
            };
        }

        public V get(K key) {
            return map.get(key);
        }

        public void remove(K key) {
            map.remove(key);
        }

        public void put(K key, V value) {
            map.put(key, value);
        }
    }

    static class CacheEntry {
        long expiry;
        byte[] value;
    }

    final LruHashMap<String, CacheEntry> map;

    public SimpleCacheSystem(int sizeLimit) {
        super();
        this.map = new LruHashMap<String, SimpleCacheSystem.CacheEntry>(sizeLimit);
    }

    @Override
    public <T> T lookup(String key, Class<T> clazz) {
        CacheEntry entry = map.get(key);
        if (entry == null) {
            return null;
        }

        if (entry.expiry != 0 && entry.expiry < System.currentTimeMillis()) {
            map.remove(key);
            return null;
        }

        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new ByteArrayInputStream(entry.value));
            T t = CastUtils.as(ois.readObject(), clazz);
            return t;
        } catch (IOException e) {
            log.warn("Ignoring error deserializing from cache", e);
            return null;
        } catch (ClassNotFoundException e) {
            log.warn("Ignoring error deserializing from cache", e);
            return null;
        } finally {
            IoUtils.safeClose(ois);
        }
    }

    @Override
    public void put(String key, TimeSpan validity, Serializable value) {
        byte[] serialized;
        ObjectOutputStream oos = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(value);
            oos.close();
            serialized = baos.toByteArray();
        } catch (IOException e) {
            throw new IllegalArgumentException("Error while serializing object", e);
        } finally {
            IoUtils.safeClose(oos);
        }

        CacheEntry entry = new CacheEntry();
        if (validity != null) {
            entry.expiry = System.currentTimeMillis() + validity.getTotalMilliseconds();
        }
        entry.value = serialized;

        map.put(key, entry);
    }

}
