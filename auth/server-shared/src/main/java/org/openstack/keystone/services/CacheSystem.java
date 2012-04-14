package org.openstack.keystone.services;

import java.io.Serializable;

import org.platformlayer.TimeSpan;

public interface CacheSystem {
	<T> T lookup(String key, Class<T> clazz);

	void put(String key, TimeSpan validity, Serializable value);
}
